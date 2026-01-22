package com.grupo_1;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor TCP para el juego de 3 en raya (Tic-Tac-Toe).
 * Acepta conexiones de clientes, gestiona el juego y sincroniza
 * el estado del tablero entre todos los clientes conectados.
 */
public class Servidor {

    private static final int PUERTO = 5000;
    private Juego juego;
    private List<HiloCliente> clientesConectados;
    private boolean servidorActivo;

    public Servidor() {
        this.clientesConectados = new ArrayList<>();
        this.juego = new Juego();
        this.servidorActivo = true;
    }

    /**
     * Inicia el servidor y se mantiene a la escucha de conexiones.
     */
    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("=================================");
            System.out.println("  SERVIDOR TIC-TAC-TOE INICIADO");
            System.out.println("  Puerto: " + PUERTO);
            System.out.println("  Esperando jugadores...");
            System.out.println("=================================");

            // Bucle principal: acepta conexiones de clientes
            while (servidorActivo) {
                try {
                    Socket socketCliente = serverSocket.accept();
                    System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());

                    // Crear y arrancar un hilo para manejar al cliente
                    HiloCliente hiloCliente = new HiloCliente(socketCliente, this);
                    clientesConectados.add(hiloCliente);
                    hiloCliente.start();

                    // Enviar el estado actual del tablero al nuevo cliente
                    hiloCliente.enviarMensaje("TABLERO " + juego.obtenerEstadoTablero());

                } catch (IOException e) {
                    if (servidorActivo) {
                        System.err.println("Error al aceptar conexión: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor en el puerto " + PUERTO);
            e.printStackTrace();
        }
    }

    /**
     * Procesa un movimiento recibido de un cliente.
     * Método sincronizado para evitar condiciones de carrera.
     * 
     * @param fila    Fila del movimiento (0-2)
     * @param columna Columna del movimiento (0-2)
     * @return El resultado del turno
     */
    public synchronized String procesarMovimiento(int fila, int columna) {
        if (!juego.esJuegoActivo()) {
            return "JUEGO_TERMINADO";
        }

        String resultado = juego.ejecutarTurno(fila, columna);

        // Enviar el estado actualizado del tablero a todos los clientes
        enviarATodos("TABLERO " + juego.obtenerEstadoTablero());

        // Si el juego terminó, enviar el resultado
        if (!juego.esJuegoActivo()) {
            enviarATodos("RESULTADO " + resultado);
        }

        return resultado;
    }

    /**
     * Envía un mensaje a todos los clientes conectados.
     * 
     * @param mensaje Mensaje a enviar
     */
    public synchronized void enviarATodos(String mensaje) {
        System.out.println("Enviando a todos: " + mensaje);
        for (HiloCliente cliente : clientesConectados) {
            cliente.enviarMensaje(mensaje);
        }
    }

    /**
     * Elimina un cliente de la lista de conectados.
     * 
     * @param cliente Cliente a eliminar
     */
    public synchronized void eliminarCliente(HiloCliente cliente) {
        clientesConectados.remove(cliente);
        System.out.println("Cliente desconectado. Clientes activos: " + clientesConectados.size());
    }

    /**
     * Reinicia el juego.
     */
    public synchronized void reiniciarJuego() {
        this.juego = new Juego();
        enviarATodos("TABLERO " + juego.obtenerEstadoTablero());
        System.out.println("Juego reiniciado.");
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();

        // Iniciar el servidor en un hilo separado (daemon para que termine con la app)
        Thread hiloServidor = new Thread(() -> servidor.iniciar());
        hiloServidor.setDaemon(true);
        hiloServidor.start();

        // Lanzar la interfaz gráfica JavaFX
        javafx.application.Application.launch(JuegoController.class, args);
    }

    // =========================================================================
    // CLASE INTERNA: HiloCliente
    // Maneja la comunicación con un cliente individual en un hilo separado.
    // =========================================================================
    private class HiloCliente extends Thread {
        private Socket socket;
        private Servidor servidor;
        private PrintWriter salida;
        private BufferedReader entrada;
        private boolean conectado;

        public HiloCliente(Socket socket, Servidor servidor) {
            this.socket = socket;
            this.servidor = servidor;
            this.conectado = true;

            try {
                this.salida = new PrintWriter(socket.getOutputStream(), true);
                this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error al crear streams: " + e.getMessage());
                conectado = false;
            }
        }

        @Override
        public void run() {
            try {
                String linea;
                while (conectado && (linea = entrada.readLine()) != null) {
                    System.out.println("Comando recibido: " + linea);
                    procesarComando(linea);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado abruptamente.");
            } finally {
                cerrarConexion();
                servidor.eliminarCliente(this);
            }
        }

        /**
         * Procesa los comandos recibidos del cliente.
         * Protocolo:
         * - PONER fila,columna : Realiza un movimiento
         * - REINICIAR : Reinicia el juego
         */
        private void procesarComando(String comando) {
            if (comando.startsWith("PONER ")) {
                // Formato: "PONER fila,columna"
                try {
                    String[] partes = comando.substring(6).split(",");
                    int fila = Integer.parseInt(partes[0].trim());
                    int columna = Integer.parseInt(partes[1].trim());

                    String resultado = servidor.procesarMovimiento(fila, columna);
                    System.out.println("Movimiento procesado: fila=" + fila + ", col=" + columna + " -> " + resultado);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    enviarMensaje("ERROR Formato inválido. Use: PONER fila,columna");
                }
            } else if (comando.equals("REINICIAR")) {
                servidor.reiniciarJuego();
            } else if (comando.equals("ESTADO")) {
                // Enviar estado actual del tablero al cliente que lo pide
                enviarMensaje("TABLERO " + juego.obtenerEstadoTablero());
            } else {
                enviarMensaje("ERROR Comando no reconocido: " + comando);
            }
        }

        /**
         * Envía un mensaje al cliente.
         * 
         * @param mensaje Mensaje a enviar
         */
        public void enviarMensaje(String mensaje) {
            if (salida != null && conectado) {
                salida.println(mensaje);
            }
        }

        /**
         * Cierra la conexión con el cliente.
         */
        public void cerrarConexion() {
            conectado = false;
            try {
                if (entrada != null)
                    entrada.close();
                if (salida != null)
                    salida.close();
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException e) {
                // Ignorar errores al cerrar
            }
        }
    }
}

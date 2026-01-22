package com.grupo_1;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor TCP para el juego de 3 en raya (Tic-Tac-Toe).
 * Gestiona dos jugadores que se conectan simultáneamente.
 * Cada jugador juega en su turno y ve las actualizaciones del otro.
 */
public class Servidor {

    private static final int PUERTO = 5000;
    private static final int MAX_JUGADORES = 2;

    private ServerSocket serverSocket;
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
        try {
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("╔═══════════════════════════════════════╗");
            System.out.println("║   SERVIDOR TIC-TAC-TOE MULTIJUGADOR   ║");
            System.out.println("╠═══════════════════════════════════════╣");
            System.out.println("║   Puerto: " + PUERTO + "                          ║");
            System.out.println("║   Esperando 2 jugadores...            ║");
            System.out.println("╚═══════════════════════════════════════╝");

            // Bucle principal: acepta conexiones de clientes
            while (servidorActivo) {
                try {
                    Socket socketCliente = serverSocket.accept();

                    // Verificar si ya tenemos 2 jugadores
                    if (clientesConectados.size() >= MAX_JUGADORES) {
                        System.out.println("Conexión rechazada: ya hay 2 jugadores");
                        PrintWriter pw = new PrintWriter(socketCliente.getOutputStream(), true);
                        pw.println("ERROR Partida llena. Ya hay 2 jugadores.");
                        socketCliente.close();
                        continue;
                    }

                    // Asignar ficha según orden de conexión
                    char ficha = (clientesConectados.size() == 0) ? 'X' : 'O';

                    // Crear y arrancar un hilo para manejar al cliente
                    HiloCliente hiloCliente = new HiloCliente(socketCliente, this, ficha);
                    clientesConectados.add(hiloCliente);
                    hiloCliente.start();

                    System.out.println("Jugador " + ficha + " conectado desde: " + socketCliente.getInetAddress());
                    System.out.println("Jugadores conectados: " + clientesConectados.size() + "/" + MAX_JUGADORES);

                    // Notificar al jugador su ficha
                    hiloCliente.enviarMensaje("BIENVENIDO " + ficha);

                    // Enviar el estado actual del tablero
                    hiloCliente.enviarMensaje("TABLERO " + juego.obtenerEstadoTablero());

                    // Si ya hay 2 jugadores, iniciar partida
                    if (clientesConectados.size() == MAX_JUGADORES) {
                        System.out.println("\n¡PARTIDA INICIADA! Turno del jugador X");
                        enviarATodos("INICIO Comienza el jugador X");
                        enviarATodos("TURNO X");
                    } else {
                        hiloCliente.enviarMensaje("ESPERA Esperando al otro jugador...");
                    }

                } catch (IOException e) {
                    if (servidorActivo) {
                        System.err.println("Error al aceptar conexión: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor en el puerto " + PUERTO);
            e.printStackTrace();
        } finally {
            detener();
        }
    }

    /**
     * Procesa un movimiento recibido de un cliente.
     * Método sincronizado para evitar condiciones de carrera.
     * 
     * @param fila    Fila del movimiento (0-2)
     * @param columna Columna del movimiento (0-2)
     * @param jugador Ficha del jugador que hace el movimiento
     * @return El resultado del turno
     */
    public synchronized String procesarMovimiento(int fila, int columna, char jugador) {
        if (!juego.esJuegoActivo()) {
            return "JUEGO_TERMINADO";
        }

        // Verificar que hay 2 jugadores
        if (clientesConectados.size() < MAX_JUGADORES) {
            return "ESPERANDO_JUGADOR";
        }

        String resultado = juego.ejecutarTurno(fila, columna, jugador);

        System.out.println("Movimiento de " + jugador + " en (" + fila + "," + columna + ") -> " + resultado);

        // Si fue un movimiento válido, enviar actualizaciones
        if (!resultado.equals("NO_ES_TU_TURNO") && !resultado.equals("ERROR_MOVIMIENTO")) {
            // Enviar el estado actualizado del tablero a todos los clientes
            enviarATodos("TABLERO " + juego.obtenerEstadoTablero());

            // Si el juego terminó, enviar el resultado
            if (!juego.esJuegoActivo()) {
                if (resultado.startsWith("GANADOR_")) {
                    char ganador = resultado.charAt(8);
                    enviarATodos("FIN GANADOR " + ganador);
                    System.out.println("\n¡PARTIDA TERMINADA! Ganador: Jugador " + ganador);
                } else if (resultado.equals("EMPATE")) {
                    enviarATodos("FIN EMPATE");
                    System.out.println("\n¡PARTIDA TERMINADA! Empate");
                }
            } else {
                // Notificar de quién es el turno
                enviarATodos("TURNO " + juego.getTurnoActual());
            }
        }

        return resultado;
    }

    /**
     * Envía un mensaje a todos los clientes conectados.
     * 
     * @param mensaje Mensaje a enviar
     */
    public synchronized void enviarATodos(String mensaje) {
        System.out.println(">> Enviando a todos: " + mensaje);
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
        System.out.println("Jugador " + cliente.getFicha() + " desconectado.");
        System.out.println("Jugadores activos: " + clientesConectados.size());

        // Notificar al otro jugador si la partida estaba en curso
        if (juego.esJuegoActivo() && clientesConectados.size() > 0) {
            enviarATodos("ABANDONO El jugador " + cliente.getFicha() + " abandonó la partida");
        }
    }

    /**
     * Reinicia el juego.
     */
    public synchronized void reiniciarJuego() {
        juego.reiniciar();
        enviarATodos("REINICIO");
        enviarATodos("TABLERO " + juego.obtenerEstadoTablero());
        enviarATodos("TURNO X");
        System.out.println("Juego reiniciado. Turno del jugador X");
    }

    /**
     * Detiene el servidor y cierra todas las conexiones.
     */
    public void detener() {
        servidorActivo = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Cerrar todas las conexiones de clientes
            for (HiloCliente cliente : clientesConectados) {
                cliente.cerrarConexion();
            }
            clientesConectados.clear();
            System.out.println("Servidor detenido.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();

        // Iniciar el servidor en un hilo separado
        Thread hiloServidor = new Thread(() -> servidor.iniciar());
        hiloServidor.setDaemon(true);
        hiloServidor.start();

        // Lanzar la interfaz gráfica JavaFX (este será el Jugador 1)
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
        private char ficha; // 'X' o 'O'

        public HiloCliente(Socket socket, Servidor servidor, char ficha) {
            this.socket = socket;
            this.servidor = servidor;
            this.ficha = ficha;
            this.conectado = true;

            try {
                this.salida = new PrintWriter(socket.getOutputStream(), true);
                this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error al crear streams: " + e.getMessage());
                conectado = false;
            }
        }

        public char getFicha() {
            return ficha;
        }

        @Override
        public void run() {
            try {
                String linea;
                while (conectado && (linea = entrada.readLine()) != null) {
                    System.out.println("<< [" + ficha + "] Comando: " + linea);
                    procesarComando(linea);
                }
            } catch (IOException e) {
                System.out.println("Jugador " + ficha + " desconectado abruptamente.");
            } finally {
                cerrarConexion();
                servidor.eliminarCliente(this);
            }
        }

        /**
         * Procesa los comandos recibidos del cliente.
         * Protocolo:
         * - PONER fila,columna : Realiza un movimiento
         * - REINICIAR : Reinicia el juego (requiere ambos jugadores)
         * - ESTADO : Solicita el estado actual
         */
        private void procesarComando(String comando) {
            if (comando.startsWith("PONER ")) {
                // Formato: "PONER fila,columna"
                try {
                    String[] partes = comando.substring(6).split(",");
                    int fila = Integer.parseInt(partes[0].trim());
                    int columna = Integer.parseInt(partes[1].trim());

                    String resultado = servidor.procesarMovimiento(fila, columna, ficha);

                    // Notificar errores específicos al cliente
                    if (resultado.equals("NO_ES_TU_TURNO")) {
                        enviarMensaje("ERROR No es tu turno. Espera al otro jugador.");
                    } else if (resultado.equals("ERROR_MOVIMIENTO")) {
                        enviarMensaje("ERROR Casilla ocupada o inválida.");
                    } else if (resultado.equals("ESPERANDO_JUGADOR")) {
                        enviarMensaje("ERROR Esperando a que se conecte el otro jugador.");
                    }

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    enviarMensaje("ERROR Formato inválido. Use: PONER fila,columna");
                }
            } else if (comando.equals("REINICIAR")) {
                servidor.reiniciarJuego();
            } else if (comando.equals("ESTADO")) {
                enviarMensaje("TABLERO " + juego.obtenerEstadoTablero());
                enviarMensaje("TURNO " + juego.getTurnoActual());
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

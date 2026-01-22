package com.grupo_1;

import java.io.*;
import java.net.Socket;

import javafx.application.Platform;

/**
 * Cliente TCP que se conecta al servidor del juego Tic-Tac-Toe.
 * Recibe actualizaciones del tablero y las aplica a la interfaz gráfica.
 */
public class Cliente {
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private JuegoDemo vista;
    private boolean conectado;
    private char miFicha; // 'X' o 'O' asignada por el servidor

    public Cliente(String ip, int puerto, JuegoDemo vista) {
        this.vista = vista;
        this.conectado = false;
        this.miFicha = ' ';

        try {
            this.socket = new Socket(ip, puerto);
            this.salida = new PrintWriter(socket.getOutputStream(), true);
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.conectado = true;

            System.out.println("Conectado al servidor: " + ip + ":" + puerto);

            // Hilo de escucha: Recibe actualizaciones del servidor sin bloquear la GUI
            Thread hiloEscucha = new Thread(this::escucharServidor);
            hiloEscucha.setDaemon(true);
            hiloEscucha.start();

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor: " + e.getMessage());
            Platform.runLater(() -> vista.setMensaje("Error: No se pudo conectar al servidor"));
        }
    }

    private void escucharServidor() {
        try {
            String linea;
            while (conectado && (linea = entrada.readLine()) != null) {
                final String mensaje = linea;
                System.out.println("Recibido del servidor: " + mensaje);
                // Usamos Platform.runLater para que los cambios en la GUI ocurran en el hilo de
                // JavaFX
                Platform.runLater(() -> procesarComando(mensaje));
            }
        } catch (IOException e) {
            System.out.println("Conexión perdida con el servidor.");
            Platform.runLater(() -> vista.setMensaje("Conexión perdida"));
        }
    }

    private void procesarComando(String comando) {
        // BIENVENIDO X - El servidor nos asigna nuestra ficha
        if (comando.startsWith("BIENVENIDO ")) {
            miFicha = comando.charAt(11);
            vista.setMiFicha(miFicha);
            vista.setMensaje("Eres el jugador " + miFicha);
            System.out.println("Mi ficha asignada: " + miFicha);
        }
        // TABLERO X-O---X-- - Estado del tablero
        else if (comando.startsWith("TABLERO ")) {
            String estado = comando.substring(8);
            actualizarTablero(estado);
        }
        // TURNO X - Indica de quién es el turno
        else if (comando.startsWith("TURNO ")) {
            char turno = comando.charAt(6);
            if (turno == miFicha) {
                vista.setMensaje("¡Tu turno! (" + miFicha + ")");
                vista.setEstiloRectangulo("-fx-fill: #90EE90"); // Verde claro
                vista.habilitarTablero();
            } else {
                vista.setMensaje("Turno del jugador " + turno);
                vista.setEstiloRectangulo("-fx-fill: #FFB6C1"); // Rosa claro
                vista.deshabilitarTablero();
            }
        }
        // ESPERA mensaje - Esperando al otro jugador
        else if (comando.startsWith("ESPERA ")) {
            String msg = comando.substring(7);
            vista.setMensaje(msg);
            vista.setEstiloRectangulo("-fx-fill: #FFFFE0"); // Amarillo claro
        }
        // INICIO mensaje - La partida comienza
        else if (comando.startsWith("INICIO ")) {
            String msg = comando.substring(7);
            vista.setMensaje(msg);
        }
        // FIN GANADOR X o FIN EMPATE - Fin de la partida
        else if (comando.startsWith("FIN ")) {
            String resultado = comando.substring(4);
            if (resultado.startsWith("GANADOR ")) {
                char ganador = resultado.charAt(8);
                if (ganador == miFicha) {
                    vista.mostrarResultado("GANASTE");
                } else {
                    vista.mostrarResultado("PERDISTE");
                }
            } else if (resultado.equals("EMPATE")) {
                vista.mostrarResultado("EMPATE");
            }
        }
        // ABANDONO mensaje - El otro jugador abandonó
        else if (comando.startsWith("ABANDONO ")) {
            String msg = comando.substring(9);
            vista.setMensaje(msg);
            vista.setEstiloRectangulo("-fx-fill: #FFA500"); // Naranja
            vista.bloquearTablero();
        }
        // REINICIO - El juego se reinició
        else if (comando.equals("REINICIO")) {
            vista.reiniciarTablero();
            vista.setMensaje("Juego reiniciado");
        }
        // ERROR mensaje - Error del servidor
        else if (comando.startsWith("ERROR ")) {
            String error = comando.substring(6);
            System.err.println("Error del servidor: " + error);
            // Mostrar error temporalmente (o podrías usar un popup)
            vista.mostrarError(error);
        }
    }

    private void actualizarTablero(String estado) {
        // Recorremos los 9 caracteres del string enviado por el servidor
        for (int i = 0; i < estado.length() && i < 9; i++) {
            char ficha = estado.charAt(i);
            vista.actualizarCasilla(i, ficha);
        }
    }

    public void enviarMovimiento(int fila, int columna) {
        if (salida != null && conectado) {
            // Enviamos el comando al servidor según el protocolo acordado
            salida.println("PONER " + fila + "," + columna);
            System.out.println("Enviado: PONER " + fila + "," + columna);
        }
    }

    public void solicitarReinicio() {
        if (salida != null && conectado) {
            salida.println("REINICIAR");
        }
    }

    public char getMiFicha() {
        return miFicha;
    }

    public boolean estaConectado() {
        return conectado;
    }

    public void cerrar() {
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

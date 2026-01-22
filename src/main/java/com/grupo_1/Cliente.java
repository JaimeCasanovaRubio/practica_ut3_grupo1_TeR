package com.grupo_1;

import java.io.*;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.image.Image;

public class Cliente {
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private JuegoDemo vista;

    public Cliente(String ip, int puerto, JuegoDemo vista) {
        this.vista = vista;
        try {
            this.socket = new Socket(ip, puerto);
            this.salida = new PrintWriter(socket.getOutputStream(), true);
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Hilo de escucha: Recibe actualizaciones del servidor sin bloquear la GUI
            new Thread(this::escucharServidor).start();

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }

    private void escucharServidor() {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                final String mensaje = linea;
                // Usamos Platform.runLater para que los cambios en la GUI ocurran en el hilo de JavaFX
                Platform.runLater(() -> procesarComando(mensaje));
            }
        } catch (IOException e) {
            System.out.println("Conexión perdida con el servidor.");
        }
    }

    private void procesarComando(String comando) {
        // Formato esperado: "TABLERO X-O---X--"
        if (comando.startsWith("TABLERO")) {
            String estado = comando.split(" ")[1];
            actualizarTablero(estado);
        }
        // Formato esperado: "RESULTADO GANASTE" o "RESULTADO EMPATE"
        else if (comando.startsWith("RESULTADO")) {
            String msg = comando.substring(10);
            // Podéis usar vuestro Label de JuegoDemo para mostrar el fin del juego
            System.out.println("Fin de la partida: " + msg);
        }
    }

    private void actualizarTablero(String estado) {
        // Recorremos los 9 caracteres del string enviado por el servidor
        for (int i = 0; i < estado.length(); i++) {
            char ficha = estado.charAt(i);

            if (ficha == 'X') {
                // j1 usa cara.png
                vista.getImagenes().get(i).setImage(new Image(getClass().getResourceAsStream("/cara.png")));
                vista.getBotones().get(i).setVisible(false);
            } else if (ficha == 'O') {
                // j2 usa cruz.png
                vista.getImagenes().get(i).setImage(new Image(getClass().getResourceAsStream("/cruz.png")));
                vista.getBotones().get(i).setVisible(false);
            }
        }
    }

    public void enviarMovimiento(int fila, int columna) {
        // Enviamos el comando al servidor según el protocolo acordado
        salida.println("PONER " + fila + "," + columna);
    }
}
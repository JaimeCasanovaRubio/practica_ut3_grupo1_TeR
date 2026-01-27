package com.grupo_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Cliente extends Application {

    private static final String HOST = "localhost";
    private static final int PORT = 3030;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage stage) throws Exception {
        // Cargar el FXML
        FXMLLoader loader = new FXMLLoader(Cliente.class.getResource("/Interfaz.FXML"));
        Scene scene = new Scene(loader.load(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/Stylesheet.css").toExternalForm());

        // Obtener el controlador
        JuegoController controller = loader.getController();

        // Conectar al servidor
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Pasar la conexión al controlador
            controller.setConexion(out, in);

            System.out.println("Conectado al servidor en " + HOST + ":" + PORT);

        } catch (IOException e) {
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
            controller.setConexion(null, null);
        }

        // Configurar ventana
        stage.setResizable(false);
        stage.setTitle("Tic-Tac-Toe - Cliente");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
        stage.setOnCloseRequest(event -> cerrarConexion());
        stage.show();
    }

    /**
     * Cierra la conexión al cerrar la ventana
     */
    private void cerrarConexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        cerrarConexion();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

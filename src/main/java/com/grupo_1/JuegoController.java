package com.grupo_1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JuegoController extends Application {

        @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(JuegoController.class.getResource("/Interfaz.FXML"));
        Scene scene = new Scene(loader.load(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/Stylesheet.css").toExternalForm());
        
        stage.setResizable(false);
        stage.setFullScreen(false);
        stage.setTitle("Tic-Tac-Toe");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

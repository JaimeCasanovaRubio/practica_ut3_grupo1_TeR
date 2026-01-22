package com.grupo_1;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class JuegoDemo implements Initializable {
    private Casilla[] tablero = new Casilla[9];
    private Jugador j1, j2;
    private Jugador turno;

    private static final int[][] LINEAS_GANADORAS = {
            { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 },
            { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
            { 0, 4, 8 }, { 2, 4, 6 }
    };

    @FXML
    private Label lblTitulo;
    @FXML
    private Rectangle rectJugador;

    @FXML
    private Button btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22;
    @FXML
    private ImageView img00, img01, img02, img10, img11, img12, img20, img21, img22;

    private Button[] botones;
    private ImageView[] imagenes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        botones = new Button[] { btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22 };
        imagenes = new ImageView[] { img00, img01, img02, img10, img11, img12, img20, img21, img22 };

        for (int i = 0; i < 9; i++)
            tablero[i] = new Casilla();

        j1 = new Jugador("j1", "/cara.png");
        j2 = new Jugador("j2", "/cruz.png");
        turno = j1;

        actualizarTurnoUI();
    }

    @FXML
    void doMarcarCasilla(ActionEvent e) {
        Button b = (Button) e.getSource();
        int index = java.util.Arrays.asList(botones).indexOf(b);

        Casilla c = tablero[index];
        if (c.estaMarcada())
            return;

        c.marcar(turno);
        b.setVisible(false);
        imagenes[index].setImage(turno.getImagen());

        if (hayGanador(turno)) {
            mostrarGanador(turno);
            return;
        }

        if (tableroLleno()) {
            mostrarEmpate();
            return;
        }

        turno = (turno == j1) ? j2 : j1;
        actualizarTurnoUI();
    }

    private boolean hayGanador(Jugador j) {
        for (int[] linea : LINEAS_GANADORAS) {
            if (j.tieneCasillas(linea))
                return true;
        }
        return false;
    }

    private boolean tableroLleno() {
        for (Casilla c : tablero)
            if (!c.estaMarcada())
                return false;
        return true;
    }

    private void mostrarGanador(Jugador j) {
        lblTitulo.setText("¡" + j.getNombre().toUpperCase() + " GANA!");
        rectJugador.setStyle("-fx-fill: gold");
        bloquearTablero();
    }

    private void mostrarEmpate() {
        lblTitulo.setText("EMPATE");
        rectJugador.setStyle("-fx-fill: lightblue");
        bloquearTablero();
    }

    private void bloquearTablero() {
        for (Button b : botones)
            b.setDisable(true);
    }

    private void actualizarTurnoUI() {
        lblTitulo.setText("TURNO DE: " + turno.getNombre().toUpperCase());
        rectJugador.setStyle(turno == j1
                ? "-fx-fill: rgba(255,250,91,100)"
                : "-fx-fill: rgba(253,124,124,100)");
    }

    private class Casilla {
        private Jugador jugador;

        boolean estaMarcada() {
            return jugador != null;
        }

        void marcar(Jugador j) {
            jugador = j;
        }

        Jugador getJugador() {
            return jugador;
        }
    }

    private class Jugador {
        private final String nombre;
        private final Image imagen;
        private final ArrayList<Integer> posiciones = new ArrayList<>();

        Jugador(String nombre, String img) {
            this.nombre = nombre;
            this.imagen = new Image(getClass().getResourceAsStream(img));
        }

        boolean tieneCasillas(int[] linea) {
            for (int p : linea)
                if (!posiciones.contains(p))
                    return false;
            return true;
        }

        void marcar(int pos) {
            posiciones.add(pos);
        }

        String getNombre() {
            return nombre;
        }

        Image getImagen() {
            return imagen;
        }
    }

}
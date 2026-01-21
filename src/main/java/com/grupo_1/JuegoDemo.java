package com.grupo_1;

import java.io.InputStream;
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

    private ArrayList<Casilla> casillas = new ArrayList<>();
    private ArrayList<Button> botones = new ArrayList<>();
    private ArrayList<ImageView> imagenes = new ArrayList<>();

    private String turno;
    private Jugador j1, j2;

    @FXML
    private Label lblTitulo;

    @FXML
    private Rectangle rectJugador;

    @FXML
    private Button btn02;

    @FXML
    private Button btn00;

    @FXML
    private Button btn01;

    @FXML
    private Button btn10;

    @FXML
    private Button btn11;

    @FXML
    private Button btn12;

    @FXML
    private Button btn20;

    @FXML
    private Button btn21;

    @FXML
    private Button btn22;

    @FXML
    private ImageView img00;

    @FXML
    private ImageView img01;

    @FXML
    private ImageView img02;

    @FXML
    private ImageView img10;

    @FXML
    private ImageView img11;

    @FXML
    private ImageView img12;

    @FXML
    private ImageView img20;

    @FXML
    private ImageView img21;

    @FXML
    private ImageView img22;

    @FXML
    void doMarcarCasilla(ActionEvent event) {
        Button b = (Button) event.getSource();
        for (Button but : botones) {
            but.setStyle("-fx-background-color: white;");
        }

        switch (b.getId()) {
            case "btn00" -> {
                Casilla c = casillas.get(0);
                if (!c.estaMarcada()) {
                    btn00.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img00.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img00.setImage(j2.getImagen());
                    }
                }
            }
            case "btn01" -> {
                Casilla c = casillas.get(1);
                if (!c.estaMarcada()) {
                    btn01.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img01.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img01.setImage(j2.getImagen());
                    }
                }
            }
            case "btn02" -> {
                Casilla c = casillas.get(2);
                if (!c.estaMarcada()) {
                    btn02.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img02.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img02.setImage(j2.getImagen());
                    }
                }
            }
            case "btn10" -> {
                Casilla c = casillas.get(3);
                if (!c.estaMarcada()) {
                    btn10.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img10.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img10.setImage(j2.getImagen());
                    }
                }
            }
            case "btn11" -> {
                Casilla c = casillas.get(4);
                if (!c.estaMarcada()) {
                    btn11.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img11.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img11.setImage(j2.getImagen());
                    }
                }
            }
            case "btn12" -> {
                Casilla c = casillas.get(5);
                if (!c.estaMarcada()) {
                    btn12.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img12.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img12.setImage(j2.getImagen());
                    }
                }
            }
            case "btn20" -> {
                Casilla c = casillas.get(6);
                if (!c.estaMarcada()) {
                    btn20.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img20.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img20.setImage(j2.getImagen());
                    }
                }
            }
            case "btn21" -> {
                Casilla c = casillas.get(7);
                if (!c.estaMarcada()) {
                    btn21.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img21.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img21.setImage(j2.getImagen());
                    }
                }
            }
            case "btn22" -> {
                Casilla c = casillas.get(8);
                if (!c.estaMarcada()) {
                    btn22.setVisible(false);
                    if (turno.equals(j1.getNombre())) {
                        c.marcar(j1);
                        img22.setImage(j1.getImagen());
                    } else {
                        c.marcar(j2);
                        img22.setImage(j2.getImagen());
                    }
                }
            }
        }

        cambiarTurno();
        comprobarGanador();
    }

    private void comprobarGanador() {
        int contador1 = j1.getCasillas().size();
        int contador2 = j2.getCasillas().size();
        Jugador ganador = null;

        if (contador1 >= 3 && contador2 < 3) {
            ArrayList<Casilla> ca = j1.getCasillas();
            System.out.println("Jugador 1 casillas: ");
            for (Casilla cas : ca) {
                System.out.println(cas.getPosicion());
            }

            if (comprobarLinea(ca)) {
                reset();
                ganador = j1;
            }
        } else if (contador2 >= 3) {
            ArrayList<Casilla> ca = j2.getCasillas();
            System.out.println("Jugador 2 casillas: ");
            for (Casilla cas : ca) {
                System.out.println(cas.getPosicion());
            }

            if (comprobarLinea(ca)) {
                reset();
                ganador = j2;
            } else {
                reset();
                btn11.setText("EMPATE");
                rectJugador.setStyle("-fx-fill: rgba(97, 214, 243, 100)");
                lblTitulo.setText("SIN JUGADAS - EMPATE -");
            }

        }
        if (ganador != null) {
            btn11.setText("JUGADOR " + ganador.getNombre().toUpperCase() + " GANA");
            rectJugador.setStyle("-fx-fill: rgba(255, 227, 117, 185)");
            lblTitulo.setText("¡"+ganador.getNombre().toUpperCase()+" HA GANADO!");
        }
    }

    private boolean comprobarLinea(ArrayList<Casilla> ca) {
        int f0 = (int) ca.get(0).getPosicion();
        int f1 = (int) ca.get(1).getPosicion();
        int f2 = (int) ca.get(2).getPosicion();

        int c0 = (int) Math.round((ca.get(0).getPosicion() - f0) * 10);
        int c1 = (int) Math.round((ca.get(1).getPosicion() - f1) * 10);
        int c2 = (int) Math.round((ca.get(2).getPosicion() - f2) * 10);

        // Comprobar fila
        if (f0 == f1 && f1 == f2)
            return true;

        // Comprobar columna
        if (c0 == c1 && c1 == c2)
            return true;

        // Comprobar diagonales
        // Diagonal principal: 0.0,1.1,2.2
        if ((f0 == 0 && c0 == 0 && f1 == 1 && c1 == 1 && f2 == 2 && c2 == 2) ||
                (f0 == 0 && c0 == 2 && f1 == 1 && c1 == 1 && f2 == 2 && c2 == 0)) { // Diagonal secundaria
            return true;
        }

        return false;
    }

    private void reset() {
        for (Button b : botones) {
            b.setVisible(true);
            b.setDisable(true);
        }
        for (ImageView i : imagenes) {
            i.setVisible(false);
        }
    }

    private void cambiarTurno() {
        if (turno.equals(j1.getNombre())) {
            rectJugador.setStyle("-fx-fill: rgba(253, 124, 124, 100)");
            turno = j2.getNombre();
        } else {
            rectJugador.setStyle("-fx-fill: rgba(255, 250, 91, 100)");
            turno = j1.getNombre();
        }
        lblTitulo.setText("TURNO DE: " + turno.toUpperCase());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i <= 22; i += 1) {
            double val = i / 10.0;
            casillas.add(new Casilla(val));

            if (i == 2) {
                i = 9;
            } else if (i == 12) {
                i = 19;
            }
        }

        j1 = new Jugador("j1", "/cara.png");
        j2 = new Jugador("j2", "/cruz.png");

        rectJugador.setStyle("-fx-fill: rgba(255, 250, 91, 100)");
        turno = j1.getNombre();
        lblTitulo.setText("TURNO DE: " + turno);

        botones.add(btn00);
        botones.add(btn01);
        botones.add(btn02);
        botones.add(btn10);
        botones.add(btn11);
        botones.add(btn12);
        botones.add(btn20);
        botones.add(btn21);
        botones.add(btn22);

        imagenes.add(img00);
        imagenes.add(img01);
        imagenes.add(img02);
        imagenes.add(img10);
        imagenes.add(img11);
        imagenes.add(img12);
        imagenes.add(img20);
        imagenes.add(img21);
        imagenes.add(img22);

        for (Button b : botones) {
            b.setText("");
        }
    }

    private class Casilla {
        private boolean marcada;
        private double posicion;

        public Casilla(double p) {
            this.posicion = p;
        }

        public void marcar(Jugador j) {
            marcada = true;
            j.nuevaCasilla(this);
        }

        public boolean estaMarcada() {
            return marcada;
        }

        public double getPosicion() {
            return posicion;
        }
    }

    private class Jugador {
        private Image imagen;
        private String nombre;
        private ArrayList<Casilla> casillas;

        public Jugador(String nombre, String urlImagen) {
            this.nombre = nombre;
            this.casillas = new ArrayList<>();

            try {
                InputStream is = getClass().getResourceAsStream(urlImagen);
                if (is != null) {
                    this.imagen = new Image(is);
                } else {
                    System.err.println("No se pudo cargar la imagen: " + urlImagen);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Image getImagen() {
            return imagen;
        }

        public String getNombre() {
            return nombre;
        }

        public ArrayList<Casilla> getCasillas() {
            return casillas;
        }

        public void nuevaCasilla(Casilla c) {
            casillas.add(c);
        }
    }
}
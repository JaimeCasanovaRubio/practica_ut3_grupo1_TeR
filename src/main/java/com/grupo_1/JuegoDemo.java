package com.grupo_1;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * Controlador de la interfaz gráfica del juego Tic-Tac-Toe multijugador.
 * Se conecta al servidor como cliente y recibe actualizaciones del tablero.
 */
public class JuegoDemo implements Initializable {

    // Cliente para comunicación con el servidor
    private Cliente cliente;

    // Imágenes para las fichas
    private Image imagenX;
    private Image imagenO;

    // Mi ficha asignada por el servidor
    private char miFicha = ' ';

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

    public Button[] getBotones() {
        return botones;
    }

    public ImageView[] getImagenes() {
        return imagenes;
    }

    /**
     * Establece la ficha asignada a este jugador.
     * 
     * @param ficha 'X' o 'O'
     */
    public void setMiFicha(char ficha) {
        this.miFicha = ficha;
    }

    /**
     * Actualiza el label de título con un mensaje.
     * 
     * @param mensaje Mensaje a mostrar
     */
    public void setMensaje(String mensaje) {
        lblTitulo.setText(mensaje);
    }

    /**
     * Cambia el color del rectángulo indicador.
     * 
     * @param estilo Estilo CSS
     */
    public void setEstiloRectangulo(String estilo) {
        rectJugador.setStyle(estilo);
    }

    /**
     * Habilita todos los botones del tablero (para casillas vacías).
     */
    public void habilitarTablero() {
        for (int i = 0; i < botones.length; i++) {
            if (botones[i].isVisible()) {
                botones[i].setDisable(false);
            }
        }
    }

    /**
     * Deshabilita todos los botones del tablero.
     */
    public void deshabilitarTablero() {
        for (Button b : botones) {
            b.setDisable(true);
        }
    }

    /**
     * Bloquea todos los botones del tablero (fin del juego).
     */
    public void bloquearTablero() {
        for (Button b : botones) {
            b.setDisable(true);
        }
    }

    /**
     * Reinicia el tablero visualmente.
     */
    public void reiniciarTablero() {
        for (int i = 0; i < 9; i++) {
            imagenes[i].setImage(null);
            botones[i].setVisible(true);
            botones[i].setDisable(false);
        }
        rectJugador.setStyle("-fx-fill: lightgray");
    }

    /**
     * Muestra un mensaje de error temporalmente.
     * 
     * @param error Mensaje de error
     */
    public void mostrarError(String error) {
        // Guardar mensaje actual
        String mensajeActual = lblTitulo.getText();
        lblTitulo.setText("⚠ " + error);

        // Restaurar después de 2 segundos
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    if (lblTitulo.getText().startsWith("⚠")) {
                        lblTitulo.setText(mensajeActual);
                    }
                });
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar arrays de botones e imágenes
        botones = new Button[] { btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22 };
        imagenes = new ImageView[] { img00, img01, img02, img10, img11, img12, img20, img21, img22 };

        // Cargar imágenes de las fichas
        imagenX = new Image(getClass().getResourceAsStream("/cara.png"));
        imagenO = new Image(getClass().getResourceAsStream("/cruz.png"));

        // Deshabilitar tablero hasta que se asigne turno
        deshabilitarTablero();

        // Mostrar mensaje inicial
        lblTitulo.setText("Conectando al servidor...");
        rectJugador.setStyle("-fx-fill: lightgray");

        // Conectarse al servidor local
        cliente = new Cliente("localhost", 5000, this);
    }

    /**
     * Maneja el clic en una casilla del tablero.
     * Envía el movimiento al servidor.
     */
    @FXML
    void doMarcarCasilla(ActionEvent e) {
        Button b = (Button) e.getSource();
        int index = java.util.Arrays.asList(botones).indexOf(b);

        // Convertir índice a fila y columna
        int fila = index / 3;
        int columna = index % 3;

        // Enviar movimiento al servidor
        if (cliente != null) {
            cliente.enviarMovimiento(fila, columna);
        }

        // Deshabilitar temporalmente hasta recibir respuesta
        deshabilitarTablero();
    }

    /**
     * Actualiza una casilla del tablero con la ficha correspondiente.
     * 
     * @param index Índice de la casilla (0-8)
     * @param ficha Carácter de la ficha ('X', 'O', o '-')
     */
    public void actualizarCasilla(int index, char ficha) {
        if (index < 0 || index >= 9)
            return;

        if (ficha == 'X') {
            imagenes[index].setImage(imagenX);
            botones[index].setVisible(false);
        } else if (ficha == 'O') {
            imagenes[index].setImage(imagenO);
            botones[index].setVisible(false);
        } else {
            // Casilla vacía
            imagenes[index].setImage(null);
            botones[index].setVisible(true);
        }
    }

    /**
     * Muestra el resultado final del juego.
     * 
     * @param resultado GANASTE, PERDISTE o EMPATE
     */
    public void mostrarResultado(String resultado) {
        switch (resultado) {
            case "GANASTE":
                lblTitulo.setText("🎉 ¡GANASTE! (" + miFicha + ")");
                rectJugador.setStyle("-fx-fill: gold");
                break;
            case "PERDISTE":
                lblTitulo.setText("😢 ¡PERDISTE!");
                rectJugador.setStyle("-fx-fill: #ff6b6b");
                break;
            case "EMPATE":
                lblTitulo.setText("🤝 EMPATE");
                rectJugador.setStyle("-fx-fill: lightblue");
                break;
            default:
                lblTitulo.setText(resultado);
        }
        bloquearTablero();
    }
}
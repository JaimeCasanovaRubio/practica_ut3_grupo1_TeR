package com.grupo_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class JuegoController implements Initializable {

    // Botones del tablero (fila-columna)
    @FXML
    private Button btn00, btn01, btn02;
    @FXML
    private Button btn10, btn11, btn12;
    @FXML
    private Button btn20, btn21, btn22;

    // ImageViews del tablero (fila-columna)
    @FXML
    private ImageView img00, img01, img02;
    @FXML
    private ImageView img10, img11, img12;
    @FXML
    private ImageView img20, img21, img22;

    // Panel lateral
    @FXML
    private Label lblTitulo;
    @FXML
    private Button btnVolverJugar;
    @FXML
    private Rectangle rectJugador;

    // Conexión con el servidor
    private PrintWriter out;
    private BufferedReader in;
    private boolean miTurno = false;
    private Character miSimbolo; // 'x' o 'o'
    private Button[][] botones;
    private ImageView[][] imagenes;

    // Imágenes para X (cruz) y O (cara)
    private Image imgCruz;
    private Image imgCara;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar imágenes
        imgCruz = new Image(getClass().getResourceAsStream("/cruz.png"));
        imgCara = new Image(getClass().getResourceAsStream("/cara.png"));

        // Organizar botones en matriz para acceso fácil
        botones = new Button[][] {
                { btn00, btn01, btn02 },
                { btn10, btn11, btn12 },
                { btn20, btn21, btn22 }
        };

        // Organizar ImageViews en matriz
        imagenes = new ImageView[][] {
                { img00, img01, img02 },
                { img10, img11, img12 },
                { img20, img21, img22 }
        };

        // Limpiar texto inicial de botones y hacer transparentes
        for (Button[] fila : botones) {
            for (Button btn : fila) {
                btn.setText("");
                btn.setStyle("-fx-background-color: transparent;");
            }
        }

        btnVolverJugar.setVisible(false);
        lblTitulo.setText("Conectando...");
    }

    /**
     * Recibe la conexión del Cliente para comunicarse con el servidor
     */
    public void setConexion(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
        iniciarEscucha();
    }

    /**
     * Inicia un hilo que escucha mensajes del servidor
     */
    private void iniciarEscucha() {
        Thread hiloEscucha = new Thread(() -> {
            try {
                String linea;
                while ((linea = in.readLine()) != null) {
                    procesarMensaje(linea);
                }
            } catch (IOException e) {
                Platform.runLater(() -> lblTitulo.setText("Conexión perdida"));
            }
        });
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    /**
     * Procesa los mensajes recibidos del servidor
     */
    private void procesarMensaje(String mensaje) {
        // Limpiar códigos ANSI de colores
        String limpio = mensaje.replaceAll("\u001B\\[[;\\d]*m", "").trim();

        Platform.runLater(() -> {
            if (limpio.contains("ESPERANDO")) {
                lblTitulo.setText("Esperando jugador...");
                rectJugador.setFill(Color.ORANGE);
            } else if (limpio.contains("TE TOCA")) {
                miTurno = true;
                lblTitulo.setText("¡Tu turno!");
                rectJugador.setFill(Color.LIGHTGREEN);
                habilitarBotones(true);
            } else if (limpio.contains("TURNO DE X")) {
                miTurno = false;
                miSimbolo = 'o'; // Si dice turno de X, yo soy O
                lblTitulo.setText("Turno del rival (X)");
                rectJugador.setFill(Color.LIGHTCORAL);
                habilitarBotones(false);
            } else if (limpio.contains("TURNO DE O")) {
                miTurno = false;
                miSimbolo = 'x'; // Si dice turno de O, yo soy X
                lblTitulo.setText("Turno del rival (O)");
                rectJugador.setFill(Color.LIGHTCORAL);
                habilitarBotones(false);
            } else if (limpio.contains("HAS GANADO")) {
                lblTitulo.setText("¡GANASTE!");
                rectJugador.setFill(Color.GOLD);
                finalizarPartida();
            } else if (limpio.contains("Has perdido")) {
                lblTitulo.setText("Perdiste :(");
                rectJugador.setFill(Color.DARKRED);
                finalizarPartida();
            } else if (limpio.contains("EMPATE")) {
                lblTitulo.setText("¡Empate!");
                rectJugador.setFill(Color.GRAY);
                finalizarPartida();
            } else if (limpio.contains("Casilla ocupada")) {
                lblTitulo.setText("Casilla ocupada");
            }
            // Actualizar tablero cuando el servidor envía el estado
            else if (limpio.startsWith("TABLERO:")) {
                String estado = limpio.substring(8); // Quitar "TABLERO:"
                actualizarTableroCompleto(estado);
            }
            // Detectar símbolo asignado al inicio
            else if (limpio.contains("BIENVENID")) {
                // Esperar a ver quién empieza para saber nuestro símbolo
            }
        });
    }

    /**
     * Se llama cuando el usuario pulsa una casilla del tablero
     */
    @FXML
    public void doMarcarCasilla(ActionEvent event) {
        if (!miTurno)
            return;

        Button botonPulsado = (Button) event.getSource();

        // Encontrar fila y columna del botón pulsado
        int fila = -1, columna = -1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (botones[i][j] == botonPulsado) {
                    fila = i + 1; // El servidor usa 1-indexed
                    columna = j + 1;
                    break;
                }
            }
        }

        if (fila != -1 && columna != -1) {
            // Enviar movimiento al servidor
            out.println(fila + " " + columna);

            // Marcar casilla localmente
            String simbolo = (miSimbolo != null && miSimbolo == 'x') ? "X" : "O";
            botonPulsado.setText(simbolo);
            botonPulsado.setDisable(true);

            miTurno = false;
            lblTitulo.setText("Esperando rival...");
            habilitarBotones(false);
        }
    }

    /**
     * Actualiza una casilla del tablero
     */
    public void actualizarCasilla(int fila, int columna, Character simbolo) {
        Platform.runLater(() -> {
            Button btn = botones[fila][columna];
            btn.setText(simbolo.toString().toUpperCase());
            btn.setDisable(true);
        });
    }

    /**
     * Actualiza todo el tablero basándose en el estado recibido del servidor
     * 
     * @param estado String de 9 caracteres (ej: "x-o------")
     */
    private void actualizarTableroCompleto(String estado) {
        if (estado.length() != 9)
            return;

        int index = 0;
        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                char c = estado.charAt(index++);
                Button btn = botones[fila][col];
                ImageView img = imagenes[fila][col];
                if (c == 'x') {
                    img.setImage(imgCruz);
                    btn.setDisable(true);
                } else if (c == 'o') {
                    img.setImage(imgCara);
                    btn.setDisable(true);
                } else {
                    img.setImage(null);
                    // No cambiar disable aquí, se maneja con habilitarBotones
                }
            }
        }
    }

    /**
     * Habilita o deshabilita los botones del tablero
     */
    private void habilitarBotones(boolean habilitar) {
        for (Button[] fila : botones) {
            for (Button btn : fila) {
                // Solo habilitar si la casilla está vacía
                if (habilitar) {
                    btn.setDisable(!btn.getText().isEmpty());
                } else {
                    btn.setDisable(true);
                }
            }
        }
    }

    /**
     * Finaliza la partida
     */
    private void finalizarPartida() {
        habilitarBotones(false);
        btnVolverJugar.setVisible(true);
        btnVolverJugar.setText("Cerrar");
    }

    /**
     * Botón para cerrar/volver a jugar
     */
    @FXML
    public void doVolverJugar(ActionEvent event) {
        Platform.exit();
    }
}

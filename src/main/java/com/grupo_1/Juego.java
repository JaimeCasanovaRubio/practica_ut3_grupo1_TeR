package com.grupo_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Juego implements Runnable {
    // COLORES
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";

    private ArrayList<Socket> jugadores = new ArrayList<>();
    private final Socket j1, j2;
    private boolean terminado = false, empate = false;
    private final Character[] casillas;
    private final ArrayList<PrintWriter> outs = new ArrayList<>();
    private final ArrayList<BufferedReader> ins = new ArrayList<>();
    private Socket turno, ganador;
    private String movimiento;
    private String[] ints;
    private int fila, columna;
    private final String MENSAJE_GANADOR = ANSI_GREEN + "\n\n¡¡FELICIDADES: HAS GANADO!!" + ANSI_RESET;
    private final String MENSAJE_PERDEDOR = ANSI_RED + "\n\nHas perdido. Más suerte la próxima vez!" + ANSI_RESET;

    public Juego(ArrayList<Socket> jugadores) {
        this.jugadores = new ArrayList<>(jugadores);
        ganador = null;
        j1 = this.jugadores.get(0);
        j2 = this.jugadores.get(1);
        casillas = new Character[9];
        for (int i = 0; i < casillas.length; i++) {
            casillas[i] = '-';
        }
    }

    @Override
    public void run() {
        try (j1;
                j2;
                BufferedReader in1 = new BufferedReader(new InputStreamReader(j1.getInputStream()));
                PrintWriter out1 = new PrintWriter(j1.getOutputStream(), true);

                BufferedReader in2 = new BufferedReader(new InputStreamReader(j2.getInputStream()));
                PrintWriter out2 = new PrintWriter(j2.getOutputStream(), true);

        ) {
            outs.add(out1);
            outs.add(out2);
            ins.add(in1);
            ins.add(in2);
            String msgOk = "OK";
            String msgBienvenida = "== BIENVENID@ AL JUEGO DE: TRES EN RAYA ==";
            String normas = """
                    Las normas del juego son sencillas:
                     - Cuando sea tu turno, escribe la casilla donde quieras colocar tu ficha.
                     - El formato es: 1 1 (fila 1 columna 1)
                     - Para ganar 3 de tus fichas deben estar en línea.
                     - Introduce 'SALIR' para dejar de juegar.

                    ¡¡Buena suerte!!
                    """;

            mensajeTodos(msgOk);
            mensajeTodos(msgBienvenida);
            mensajeTodos(normas);

            turno = j1;

            while (!terminado) {
                pedirMovimiento();
                comprobarGanador();
                if (!terminado) {
                    if (turno == j1)
                        turno = j2;
                    else
                        turno = j1;
                } else {
                    dibujarTablero();
                    mensajeTodos("TERMINADO");
                }
            }

        } catch (IOException e) {
            mensajeTodos("\nERROR DEL SERVIDOR");
        }
    }

    private void comprobarGanador() {
        Character cGanador = ' ';
        for (int i = 0; i < 5; i++) {
            if (casillas[i] != '-') {
                switch (i) {
                case 0 -> {
                    if ((casillas[i].equals(casillas[i + 1]) && casillas[i].equals(casillas[i + 2]))
                            || (casillas[i].equals(casillas[i + 3]) && casillas[i].equals(casillas[i + 6]))
                            || (casillas[i].equals(casillas[i + 4]) && casillas[i].equals(casillas[i + 8]))) {
                        cGanador = casillas[i];
                    }
                }
                case 1 -> {
                    if ((casillas[i].equals(casillas[i + 1]) && casillas[i].equals(casillas[i - 1]))
                            || (casillas[i].equals(casillas[i + 3]) && casillas[i].equals(casillas[i + 6]))) {
                        cGanador = casillas[i];
                    }
                }
                case 2 -> {
                    if ((casillas[i].equals(casillas[i - 1]) && casillas[i].equals(casillas[i - 2]))
                            || (casillas[i].equals(casillas[i + 3]) && casillas[i].equals(casillas[i + 6]))
                            || (casillas[i].equals(casillas[i + 2]) && casillas[i].equals(casillas[i + 4]))) {
                        cGanador = casillas[i];
                    }
                }
                case 3 -> {
                    if ((casillas[i].equals(casillas[i + 1]) && casillas[i].equals(casillas[i + 2]))
                            || (casillas[i].equals(casillas[i + 3]) && casillas[i].equals(casillas[i - 3]))) {
                        cGanador = casillas[i];
                    }
                }
                case 6 -> {
                    if ((casillas[i].equals(casillas[i + 1]) && casillas[i].equals(casillas[i + 2]))
                            || (casillas[i].equals(casillas[i - 3]) && casillas[i].equals(casillas[i - 6]))) {
                        cGanador = casillas[i];
                    }
                }
                }
            }
        }

        if (cGanador.equals('x')) {
            ganador = j1;
            outs.get(0).println(MENSAJE_GANADOR);
            outs.get(1).println(MENSAJE_PERDEDOR);
        } else if (cGanador.equals('o')) {
            ganador = j2;
            outs.get(1).println(MENSAJE_GANADOR);
            outs.get(0).println(MENSAJE_PERDEDOR);
        }

        for (Character casilla : casillas) {
            if (casilla.equals('-')) {
                empate = false;
                break;
            }
            empate = true;
        }

        if (empate) {
            mensajeTodos(ANSI_YELLOW + "\n== EMPATE ==" + ANSI_RESET);
        }

        if (ganador != null || empate) {
            terminado = true;
        }
    }

    private void pedirMovimiento() {
        boolean ocupada = false;

        do {
            movimiento = "";
            ints = new String[2];
            fila = 0;
            columna = 0;
            int posicionCasilla = 0;

            if (turno == j1) {
                outs.get(0).println(ANSI_BLUE + "\n== TE TOCA ==" + ANSI_RESET);
                outs.get(1).println(ANSI_BLUE + "\n TURNO DE X  " + ANSI_RESET);
                dibujarTablero();
                comprobarMovimiento(outs.get(0), ins.get(0));
            } else {
                outs.get(1).println(ANSI_BLUE + "\n== TE TOCA ==" + ANSI_RESET);
                outs.get(0).println(ANSI_BLUE + "\n TURNO DE O  " + ANSI_RESET);
                dibujarTablero();
                comprobarMovimiento(outs.get(1), ins.get(1));
            }

            fila = Integer.parseInt(ints[0]);
            columna = Integer.parseInt(ints[1]);
            switch (fila) {
            case 1 -> posicionCasilla = columna - 1;
            case 2 -> posicionCasilla = columna + fila;
            case 3 -> posicionCasilla = columna + fila + 2;
            }

            if (!casillas[posicionCasilla].equals('-')) {
                ocupada = true;
                if (turno == j1)
                    outs.get(0).println(ANSI_RED + "\nCasilla ocupada. Elige otra" + ANSI_RESET);
                else
                    outs.get(1).println(ANSI_RED + "\nCasilla ocupada. Elige otra" + ANSI_RESET);

            } else {
                ocupada = false;
                if (turno == j1)
                    casillas[posicionCasilla] = 'x';
                else
                    casillas[posicionCasilla] = 'o';
            }

        } while (ocupada);

    }

    private void comprobarMovimiento(PrintWriter pw, BufferedReader br) {
        boolean error = false;
        try {
            do {
                pw.println("> ");
                movimiento = br.readLine();
                if (movimiento.equalsIgnoreCase("SALIR")){
                    terminado = true;
                    for (PrintWriter p: outs){
                        p.println(ANSI_RED+"\n*Juego cancelado*"+ANSI_RESET);
                    }
                    break;
                }
                if (movimiento.length() != 3) {
                    error = true;
                    pw.println(ANSI_RED + "\n*FORMATO INCORRECTO* Ejemplo válido: 1 1 (fila 1 columna 1)" + ANSI_RESET);
                } else {
                    ints = movimiento.split(" ");
                    if (ints.length != 2) {
                        error = true;
                        pw.println(ANSI_RED + "\n*FORMATO INCORRECTO* Te ha faltado el espacio entre los números" + ANSI_RESET);
                    } else {
                        try {
                            fila = Integer.parseInt(ints[0]);
                            columna = Integer.parseInt(ints[1]);
                        } catch (NumberFormatException e) {
                            pw.println(ANSI_RED + "\n*FORMATO INCORRECTO* Introduce números enteros" + ANSI_RESET);
                        }
                        if (fila > 3 || fila < 1 || columna > 3 || columna < 1) {
                            pw.println(ANSI_RED + "*ERROR* Sólo hay 3 columnas y 3 filas. Por favor, introduce un número válido" + ANSI_RESET);
                            error = true;
                        } else {
                            error = false;
                        }
                    }
                }
            } while (error);
        } catch (IOException e) {
            System.out.println("ERROR AL COMPROBAR MOVIMIENTO: " + e.getMessage());
        }
    }

    private void mensajeTodos(String mensaje) {
        for (PrintWriter pw : outs) {
            pw.println(ANSI_BLUE + mensaje + ANSI_RESET);
        }
    }

    private void dibujarTablero() {
        for (PrintWriter pw : outs) {

            pw.println(ANSI_WHITE + "    1   2   3");

            int c = 0;

            for (int fila = 1; fila <= 3; fila++) {
                pw.print(" " + fila + " ");

                pw.print(" " + casillas[c++] + " |");
                pw.print(" " + casillas[c++] + " |");
                pw.println(" " + casillas[c++]);

                if (fila < 3) {
                    pw.println("   ---+---+---" + ANSI_RESET);
                }
            }
            pw.println();
        }
    }

}

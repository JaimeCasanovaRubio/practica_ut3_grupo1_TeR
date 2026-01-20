package com.grupo_1;

public class Juego {
    private char[] tablero; // Array de 9 posiciones (0-8)
    private boolean juegoActivo;

    public Juego() {
        tablero = new char[9];
        for (int i = 0; i < 9; i++) {
            tablero[i] = '-'; // Representa casilla vacía
        }
        this.juegoActivo = true;
    }

    /**
     * Procesa el turno del jugador y el de la CPU.
     * @return El estado de la partida tras los movimientos.
     */
    public String ejecutarTurno(int fila, int columna) {
        int indice = (fila * 3) + columna;

        // 1. Validar movimiento
        if (indice < 0 || indice > 8 || tablero[indice] != '-') {
            return "ERROR MOVIMIENTO";
        }

        // 2. Movimiento del Jugador (X)
        tablero[indice] = 'X';

        if (comprobarGanador('X')) {
            juegoActivo = false;
            return "GANASTE";
        }

        if (estaTableroLleno()) {
            juegoActivo = false;
            return "EMPATE";
        }

        // 3. Movimiento de la CPU (O)
        realizarMovimientoCPU();

        if (comprobarGanador('O')) {
            juegoActivo = false;
            return "PERDISTE";
        }

        if (estaTableroLleno()) {
            juegoActivo = false;
            return "EMPATE";
        }

        return "CONTINUA";
    }

    private void realizarMovimientoCPU() {
        // Busca la primera casilla libre
        for (int i = 0; i < 9; i++) {
            if (tablero[i] == '-') {
                tablero[i] = 'O';
                break;
            }
        }
    }

    public boolean comprobarGanador(char jugador) {
        // Combinaciones posibles de victoria
        int[][] combinaciones = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Horizontales
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Verticales
                {0, 4, 8}, {2, 4, 6}             // Diagonales
        };

        for (int[] c : combinaciones) {
            if (tablero[c[0]] == jugador &&
                    tablero[c[1]] == jugador &&
                    tablero[c[2]] == jugador) {
                return true;
            }
        }
        return false;
    }

    public boolean estaTableroLleno() {
        for (char casilla : tablero) {
            if (casilla == '-') return false;
        }
        return true;
    }

    public String obtenerEstadoTablero() {
        return new String(tablero);
    }

    public boolean esJuegoActivo() {
        return juegoActivo;
    }
}
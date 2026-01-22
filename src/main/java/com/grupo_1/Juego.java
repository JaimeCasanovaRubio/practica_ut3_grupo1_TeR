package com.grupo_1;

/**
 * Lógica del juego Tic-Tac-Toe para dos jugadores.
 * No incluye CPU - son dos jugadores humanos que alternan turnos.
 */
public class Juego {
    private char[] tablero; // Array de 9 posiciones (0-8)
    private boolean juegoActivo;
    private char turnoActual; // 'X' o 'O'

    public Juego() {
        tablero = new char[9];
        for (int i = 0; i < 9; i++) {
            tablero[i] = '-'; // Representa casilla vacía
        }
        this.juegoActivo = true;
        this.turnoActual = 'X'; // X siempre empieza
    }

    /**
     * Obtiene el jugador que tiene el turno actual.
     * 
     * @return 'X' o 'O'
     */
    public char getTurnoActual() {
        return turnoActual;
    }

    /**
     * Procesa el turno del jugador.
     * 
     * @param fila    Fila (0-2)
     * @param columna Columna (0-2)
     * @param jugador Ficha del jugador ('X' o 'O')
     * @return El estado de la partida tras el movimiento.
     */
    public String ejecutarTurno(int fila, int columna, char jugador) {
        // Verificar que es el turno correcto
        if (jugador != turnoActual) {
            return "NO_ES_TU_TURNO";
        }

        int indice = (fila * 3) + columna;

        // Validar movimiento
        if (indice < 0 || indice > 8 || tablero[indice] != '-') {
            return "ERROR_MOVIMIENTO";
        }

        // Realizar movimiento
        tablero[indice] = jugador;

        // Comprobar victoria
        if (comprobarGanador(jugador)) {
            juegoActivo = false;
            return "GANADOR_" + jugador;
        }

        // Comprobar empate
        if (estaTableroLleno()) {
            juegoActivo = false;
            return "EMPATE";
        }

        // Cambiar turno
        turnoActual = (turnoActual == 'X') ? 'O' : 'X';

        return "CONTINUA";
    }

    /**
     * Método legacy para compatibilidad (sin especificar jugador).
     * Usa el turno actual.
     */
    public String ejecutarTurno(int fila, int columna) {
        return ejecutarTurno(fila, columna, turnoActual);
    }

    public boolean comprobarGanador(char jugador) {
        // Combinaciones posibles de victoria
        int[][] combinaciones = {
                { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, // Horizontales
                { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, // Verticales
                { 0, 4, 8 }, { 2, 4, 6 } // Diagonales
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
            if (casilla == '-')
                return false;
        }
        return true;
    }

    public String obtenerEstadoTablero() {
        return new String(tablero);
    }

    public boolean esJuegoActivo() {
        return juegoActivo;
    }

    /**
     * Reinicia el juego.
     */
    public void reiniciar() {
        for (int i = 0; i < 9; i++) {
            tablero[i] = '-';
        }
        this.juegoActivo = true;
        this.turnoActual = 'X';
    }
}
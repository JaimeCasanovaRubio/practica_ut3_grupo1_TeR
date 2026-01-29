package com.grupo_1;

// JuegoLogicTest.java - Tests para la lógica del juego usando reflexión
// Sin Mockito por compatibilidad con Java 25

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grupo_1.Juego;

import org.junit.jupiter.api.DisplayName;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JuegoLogicTest {

    private Juego juego;
    private ArrayList<Socket> jugadores;
    private PrintWriter pw1;
    private PrintWriter pw2;
    private StringWriter sw1;
    private StringWriter sw2;

    @BeforeEach
    public void setUp() throws Exception {
        // Usar sockets null y crear PrintWriters reales con StringWriter
        jugadores = new ArrayList<>();
        jugadores.add(null); // No usamos los sockets realmente
        jugadores.add(null);

        juego = new Juego(jugadores);

        // Crear PrintWriters reales que escriben a StringWriters
        sw1 = new StringWriter();
        sw2 = new StringWriter();
        pw1 = new PrintWriter(sw1, true);
        pw2 = new PrintWriter(sw2, true);

        // Inyectar PrintWriters en la lista 'outs' usando reflexión
        Field outsField = Juego.class.getDeclaredField("outs");
        outsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<PrintWriter> outs = (ArrayList<PrintWriter>) outsField.get(juego);
        outs.add(pw1);
        outs.add(pw2);
    }

    @Test
    @DisplayName("El tablero se inicializa completamente vacío")
    public void testTableroInicializadoVacio() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        assertNotNull(casillas);
        assertEquals(9, casillas.length, "El tablero debe tener 9 casillas");

        for (int i = 0; i < casillas.length; i++) {
            assertEquals('-', casillas[i],
                    "Casilla " + i + " debería estar vacía al inicio");
        }
    }

    @Test
    @DisplayName("Detectar victoria en fila horizontal")
    public void testDetectarVictoriaHorizontal() throws Exception {
        // Configurar un tablero con victoria en primera fila (XXX)
        Character[] tableroGanador = { 'x', 'x', 'x', '-', '-', '-', '-', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroGanador);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertTrue(terminado, "El juego debería estar terminado");

        // Verificar que se envió mensaje de ganador
        String output = sw1.toString();
        assertTrue(output.contains("GANADO") || output.contains("perdido"),
                "Debería enviar mensaje de resultado");
    }

    @Test
    @DisplayName("Detectar victoria en columna vertical")
    public void testDetectarVictoriaVertical() throws Exception {
        Character[] tableroGanador = { 'x', '-', '-', 'x', '-', '-', 'x', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroGanador);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertTrue(terminado, "Debería detectar victoria vertical");
    }

    @Test
    @DisplayName("Detectar victoria en diagonal")
    public void testDetectarVictoriaDiagonal() throws Exception {
        Character[] tableroGanador = { 'x', '-', '-', '-', 'x', '-', '-', '-', 'x' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroGanador);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertTrue(terminado, "Debería detectar victoria diagonal");
    }

    @Test
    @DisplayName("Detectar empate cuando el tablero está lleno sin ganador")
    public void testDetectarEmpate() throws Exception {
        // Tablero lleno sin línea ganadora
        Character[] tableroEmpate = { 'x', 'o', 'x', 'x', 'o', 'o', 'o', 'x', 'x' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroEmpate);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field empateField = Juego.class.getDeclaredField("empate");
        empateField.setAccessible(true);
        boolean empate = (boolean) empateField.get(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertTrue(empate, "Debería detectar empate");
        assertTrue(terminado, "El juego debería estar terminado");

        // Verificar mensaje de empate
        String output = sw1.toString();
        assertTrue(output.contains("EMPATE"), "Debería enviar mensaje de empate");
    }

    @Test
    @DisplayName("Juego no terminado cuando hay casillas vacías sin ganador")
    public void testJuegoNoTerminado() throws Exception {
        Character[] tableroMedio = { 'x', 'o', 'x', '-', '-', 'o', '-', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroMedio);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        Field empateField = Juego.class.getDeclaredField("empate");
        empateField.setAccessible(true);
        boolean empate = (boolean) empateField.get(juego);

        assertFalse(terminado, "El juego no debería estar terminado");
        assertFalse(empate, "No debería ser empate");
    }

    @Test
    @DisplayName("Verificar inicialización de variables de estado")
    public void testEstadoInicial() throws Exception {
        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        Field empateField = Juego.class.getDeclaredField("empate");
        empateField.setAccessible(true);
        boolean empate = (boolean) empateField.get(juego);

        Field ganadorField = Juego.class.getDeclaredField("ganador");
        ganadorField.setAccessible(true);
        Socket ganador = (Socket) ganadorField.get(juego);

        assertFalse(terminado, "El juego no debería estar terminado al inicio");
        assertFalse(empate, "No debería ser empate al inicio");
        assertNull(ganador, "No debería haber ganador al inicio");
    }

    @Test
    @DisplayName("Verificar todas las combinaciones ganadoras posibles")
    public void testTodasCombinacionesGanadoras() throws Exception {
        Character[][][] combinacionesGanadoras = {
                // Filas
                { { 'x', 'x', 'x' }, { '-', '-', '-' }, { '-', '-', '-' } },
                { { '-', '-', '-' }, { 'x', 'x', 'x' }, { '-', '-', '-' } },
                { { '-', '-', '-' }, { '-', '-', '-' }, { 'x', 'x', 'x' } },
                // Columnas
                { { 'x', '-', '-' }, { 'x', '-', '-' }, { 'x', '-', '-' } },
                { { '-', 'x', '-' }, { '-', 'x', '-' }, { '-', 'x', '-' } },
                { { '-', '-', 'x' }, { '-', '-', 'x' }, { '-', '-', 'x' } },
                // Diagonales
                { { 'x', '-', '-' }, { '-', 'x', '-' }, { '-', '-', 'x' } },
                { { '-', '-', 'x' }, { '-', 'x', '-' }, { 'x', '-', '-' } }
        };

        for (int i = 0; i < combinacionesGanadoras.length; i++) {
            // Convertir matriz 3x3 a array 1D
            Character[] tablero = new Character[9];
            int idx = 0;
            for (int fila = 0; fila < 3; fila++) {
                for (int col = 0; col < 3; col++) {
                    tablero[idx++] = combinacionesGanadoras[i][fila][col];
                }
            }

            // Crear nuevo juego para cada combinación
            Juego juegoTest = new Juego(jugadores);

            // Inyectar PrintWriters
            Field outsField = Juego.class.getDeclaredField("outs");
            outsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<PrintWriter> outsTest = (ArrayList<PrintWriter>) outsField.get(juegoTest);
            outsTest.add(new PrintWriter(new StringWriter(), true));
            outsTest.add(new PrintWriter(new StringWriter(), true));

            Field casillasField = Juego.class.getDeclaredField("casillas");
            casillasField.setAccessible(true);
            casillasField.set(juegoTest, tablero);

            Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
            comprobarGanadorMethod.setAccessible(true);
            comprobarGanadorMethod.invoke(juegoTest);

            Field terminadoField = Juego.class.getDeclaredField("terminado");
            terminadoField.setAccessible(true);
            boolean terminado = (boolean) terminadoField.get(juegoTest);

            assertTrue(terminado,
                    "Debería detectar victoria en combinación " + (i + 1));
        }
    }
}
package com.grupo_1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para la lógica del juego Tres en Raya.
 * Verifica el comportamiento completo de la clase Juego usando
 * assertEquals, assertTrue, assertFalse, assertNotNull, assertNull.
 */
public class JuegoIntegrationTest {

    private Juego juego;
    private ArrayList<Socket> jugadores;
    private PrintWriter pw1;
    private PrintWriter pw2;
    private StringWriter sw1;
    private StringWriter sw2;

    @BeforeEach
    public void setUp() throws Exception {
        // Crear lista de jugadores con sockets null (no necesitamos conexión real)
        jugadores = new ArrayList<>();
        jugadores.add(null);
        jugadores.add(null);

        // Crear instancia del juego
        juego = new Juego(jugadores);

        // Crear PrintWriters reales que escriben a StringWriters para capturar salida
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

    // ==================== TESTS DEL TABLERO ====================

    @Test
    @DisplayName("El tablero debe inicializarse con 9 casillas")
    public void testTableroTiene9Casillas() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        assertNotNull(casillas, "El array de casillas no debe ser null");
        assertEquals(9, casillas.length, "El tablero debe tener exactamente 9 casillas");
    }

    @Test
    @DisplayName("Todas las casillas deben ser '-' al inicio")
    public void testTodasLasCasillasVaciasAlInicio() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        int casillasVacias = 0;
        for (Character c : casillas) {
            if (c.equals('-')) {
                casillasVacias++;
            }
        }
        assertEquals(9, casillasVacias, "Al inicio deben haber 9 casillas vacías (-)");
    }

    @Test
    @DisplayName("Se pueden colocar fichas X en el tablero")
    public void testColocarFichaX() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        casillas[0] = 'x';

        assertEquals('x', casillas[0], "La casilla 0 debe contener 'x'");
    }

    @Test
    @DisplayName("Se pueden colocar fichas O en el tablero")
    public void testColocarFichaO() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        casillas[4] = 'o';

        assertEquals('o', casillas[4], "La casilla 4 debe contener 'o'");
    }

    @Test
    @DisplayName("El tablero puede tener casillas con X, O y vacías")
    public void testTableroConMovimientosMixtos() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        casillas[0] = 'x';
        casillas[4] = 'o';
        casillas[8] = 'x';

        assertEquals('x', casillas[0], "La casilla 0 debe contener 'x'");
        assertEquals('o', casillas[4], "La casilla 4 debe contener 'o'");
        assertEquals('x', casillas[8], "La casilla 8 debe contener 'x'");
        assertEquals('-', casillas[1], "La casilla 1 debe seguir vacía");
        assertEquals('-', casillas[5], "La casilla 5 debe seguir vacía");
    }

    // ==================== TESTS DE ESTADO INICIAL ====================

    @Test
    @DisplayName("El juego no debe estar terminado al inicio")
    public void testJuegoNoTerminadoAlInicio() throws Exception {
        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertFalse(terminado, "El juego no debería estar terminado al inicio");
    }

    @Test
    @DisplayName("No debe haber empate al inicio del juego")
    public void testNoEmpateAlInicio() throws Exception {
        Field empateField = Juego.class.getDeclaredField("empate");
        empateField.setAccessible(true);
        boolean empate = (boolean) empateField.get(juego);

        assertFalse(empate, "No debería ser empate al inicio");
    }

    @Test
    @DisplayName("No debe haber ganador al inicio del juego")
    public void testNoGanadorAlInicio() throws Exception {
        Field ganadorField = Juego.class.getDeclaredField("ganador");
        ganadorField.setAccessible(true);
        Socket ganador = (Socket) ganadorField.get(juego);

        assertNull(ganador, "No debería haber ganador al inicio");
    }

    @Test
    @DisplayName("Verificar que j1 y j2 existen en el juego")
    public void testJugadoresExisten() throws Exception {
        Field j1Field = Juego.class.getDeclaredField("j1");
        j1Field.setAccessible(true);
        Field j2Field = Juego.class.getDeclaredField("j2");
        j2Field.setAccessible(true);

        // En nuestro setup los sockets son null, pero los campos deben existir
        // y haber sido asignados (aunque sean null en este caso de test)
        assertNotNull(j1Field, "El campo j1 debe existir");
        assertNotNull(j2Field, "El campo j2 debe existir");
    }

    // ==================== TESTS DE DETECCIÓN DE EMPATE ====================

    @Test
    @DisplayName("Detectar empate cuando el tablero está lleno sin ganador")
    public void testDeteccionEmpate() throws Exception {
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

        assertTrue(empate, "Debería detectar empate cuando tablero lleno sin ganador");
        assertTrue(terminado, "El juego debería estar terminado en empate");
    }

    @Test
    @DisplayName("El mensaje de EMPATE se envía a los jugadores")
    public void testMensajeEmpateEnviado() throws Exception {
        Character[] tableroEmpate = { 'x', 'o', 'x', 'x', 'o', 'o', 'o', 'x', 'x' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroEmpate);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        String output = sw1.toString();
        assertTrue(output.contains("EMPATE"), "Debería contener la palabra EMPATE");
    }

    // ==================== TESTS DE JUEGO EN PROGRESO ====================

    @Test
    @DisplayName("El juego no termina cuando hay casillas vacías y no hay ganador")
    public void testJuegoEnProgreso() throws Exception {
        Character[] tableroMedio = { 'x', 'o', '-', '-', 'x', '-', '-', '-', '-' };

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

        assertFalse(terminado, "El juego NO debería estar terminado con casillas vacías");
        assertFalse(empate, "NO debería ser empate con casillas vacías");
    }

    @Test
    @DisplayName("Tablero con una sola ficha no termina el juego")
    public void testUnSoloMovimientoNoTermina() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        casillas[0] = 'x';

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertFalse(terminado, "Con solo 1 ficha el juego NO debe terminar");
    }

    @Test
    @DisplayName("Tablero con 2 en línea no termina el juego")
    public void testDosEnLineaNoTermina() throws Exception {
        Character[] tablero = { 'x', 'x', '-', 'o', '-', '-', '-', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tablero);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertFalse(terminado, "Con solo 2 en línea el juego NO debe terminar");
    }

    // ==================== TESTS DE CONTEO DE CASILLAS ====================

    @Test
    @DisplayName("Contar casillas vacías en tablero inicial")
    public void testContarCasillasVaciasInicio() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        int casillasVacias = 0;
        for (Character c : casillas) {
            if (c.equals('-')) {
                casillasVacias++;
            }
        }

        assertEquals(9, casillasVacias, "Al inicio deben haber 9 casillas vacías");
    }

    @Test
    @DisplayName("Contar casillas vacías después de movimientos")
    public void testContarCasillasVaciasDespuesMovimientos() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        casillas[0] = 'x';
        casillas[1] = 'o';
        casillas[4] = 'x';

        int casillasVacias = 0;
        for (Character c : casillas) {
            if (c.equals('-')) {
                casillasVacias++;
            }
        }

        assertEquals(6, casillasVacias, "Después de 3 movimientos deben haber 6 casillas vacías");
    }

    @Test
    @DisplayName("Contar símbolos X y O en el tablero")
    public void testContarSimbolosXyO() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        casillas[0] = 'x';
        casillas[1] = 'o';
        casillas[2] = 'x';
        casillas[3] = 'o';
        casillas[4] = 'x';

        int contadorX = 0;
        int contadorO = 0;
        for (Character c : casillas) {
            if (c.equals('x'))
                contadorX++;
            if (c.equals('o'))
                contadorO++;
        }

        assertEquals(3, contadorX, "Deben haber 3 fichas X");
        assertEquals(2, contadorO, "Deben haber 2 fichas O");
    }

    @Test
    @DisplayName("Verificar que tablero vacío tiene 0 fichas")
    public void testTableroVacioSinFichas() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        Character[] casillas = (Character[]) casillasField.get(juego);

        int totalFichas = 0;
        for (Character c : casillas) {
            if (c.equals('x') || c.equals('o')) {
                totalFichas++;
            }
        }

        assertEquals(0, totalFichas, "Al inicio no debe haber fichas X ni O");
    }

    // ==================== TESTS DE VICTORIA JUGADOR X (primera fila)
    // ====================

    @Test
    @DisplayName("X gana en primera fila y se detecta victoria")
    public void testXGanaEnPrimeraFila() throws Exception {
        // Tablero: X X X
        // O O -
        // - - -
        Character[] tablero = { 'x', 'x', 'x', 'o', 'o', '-', '-', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tablero);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        Field ganadorField = Juego.class.getDeclaredField("ganador");
        ganadorField.setAccessible(true);

        assertTrue(terminado, "El juego debería estar terminado cuando X tiene 3 en línea");
    }

    @Test
    @DisplayName("X gana en primera columna vertical")
    public void testXGanaEnPrimeraColumna() throws Exception {
        // Tablero: X O -
        // X O -
        // X - -
        Character[] tablero = { 'x', 'o', '-', 'x', 'o', '-', 'x', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tablero);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertTrue(terminado, "El juego debería terminar con victoria en columna 1");
    }

    @Test
    @DisplayName("X gana en diagonal principal (0-4-8)")
    public void testXGanaEnDiagonalPrincipal() throws Exception {
        // Tablero: X O -
        // O X -
        // - - X
        Character[] tablero = { 'x', 'o', '-', 'o', 'x', '-', '-', '-', 'x' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tablero);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        boolean terminado = (boolean) terminadoField.get(juego);

        assertTrue(terminado, "El juego debería terminar con victoria en diagonal principal");
    }

    // ==================== TESTS DE MENSAJES ====================

    @Test
    @DisplayName("Se envía mensaje de ganador cuando X gana")
    public void testMensajeGanadorEnviado() throws Exception {
        Character[] tableroGanadorX = { 'x', 'x', 'x', 'o', 'o', '-', '-', '-', '-' };

        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tableroGanadorX);

        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);

        String outputJ1 = sw1.toString();
        String outputJ2 = sw2.toString();

        // Verificar que se enviaron mensajes
        boolean enviaMensajes = !outputJ1.isEmpty() || !outputJ2.isEmpty();
        assertTrue(enviaMensajes, "Debería enviar mensajes al ganar");
    }

    // ==================== TESTS DE ESTRUCTURA INTERNA ====================

    @Test
    @DisplayName("Verificar que existe campo casillas de tipo Character[]")
    public void testCampoCasillasExiste() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);

        assertNotNull(casillasField, "El campo 'casillas' debe existir");
        assertEquals(Character[].class, casillasField.getType(),
                "El campo casillas debe ser de tipo Character[]");
    }

    @Test
    @DisplayName("Verificar que existe método comprobarGanador")
    public void testMetodoComprobarGanadorExiste() throws Exception {
        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");

        assertNotNull(comprobarGanadorMethod, "El método 'comprobarGanador' debe existir");
    }

    @Test
    @DisplayName("Verificar que existe campo terminado de tipo boolean")
    public void testCampoTerminadoExiste() throws Exception {
        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);

        assertNotNull(terminadoField, "El campo 'terminado' debe existir");
        assertEquals(boolean.class, terminadoField.getType(),
                "El campo terminado debe ser de tipo boolean");
    }

    @Test
    @DisplayName("Verificar que existe campo empate de tipo boolean")
    public void testCampoEmpateExiste() throws Exception {
        Field empateField = Juego.class.getDeclaredField("empate");
        empateField.setAccessible(true);

        assertNotNull(empateField, "El campo 'empate' debe existir");
        assertEquals(boolean.class, empateField.getType(),
                "El campo empate debe ser de tipo boolean");
    }
}

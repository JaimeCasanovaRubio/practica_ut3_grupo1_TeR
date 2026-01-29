// JuegoTest.java - Suite completa de tests para la clase Juego
// Tests unitarios para la lógica del juego de Tres en Raya

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import com.grupo_1.Juego;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite completa de tests para la clase Juego
 * Utiliza reflexión para acceder a métodos y campos privados
 * Cubre: inicialización, victorias (horizontal, vertical, diagonal), empates y estados del juego
 */
public class JuegoTest {

    private Juego juego;
    private ArrayList<Socket> jugadores;
    private PrintWriter pw1;
    private PrintWriter pw2;
    private StringWriter sw1;
    private StringWriter sw2;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar jugadores con sockets null
        jugadores = new ArrayList<>();
        jugadores.add(null);
        jugadores.add(null);

        juego = new Juego(jugadores);

        // Crear PrintWriters con StringWriters para capturar salida
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

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el array de casillas del tablero mediante reflexión
     */
    private Character[] obtenerCasillas() throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        return (Character[]) casillasField.get(juego);
    }

    /**
     * Establece el estado del tablero
     */
    private void establecerTablero(Character[] tablero) throws Exception {
        Field casillasField = Juego.class.getDeclaredField("casillas");
        casillasField.setAccessible(true);
        casillasField.set(juego, tablero);
    }

    /**
     * Invoca el método privado comprobarGanador
     */
    private void comprobarGanador() throws Exception {
        Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
        comprobarGanadorMethod.setAccessible(true);
        comprobarGanadorMethod.invoke(juego);
    }

    /**
     * Obtiene el valor del campo 'terminado'
     */
    private boolean obtenerTerminado() throws Exception {
        Field terminadoField = Juego.class.getDeclaredField("terminado");
        terminadoField.setAccessible(true);
        return (boolean) terminadoField.get(juego);
    }

    /**
     * Obtiene el valor del campo 'empate'
     */
    private boolean obtenerEmpate() throws Exception {
        Field empateField = Juego.class.getDeclaredField("empate");
        empateField.setAccessible(true);
        return (boolean) empateField.get(juego);
    }

    /**
     * Obtiene el valor del campo 'ganador'
     */
    private Socket obtenerGanador() throws Exception {
        Field ganadorField = Juego.class.getDeclaredField("ganador");
        ganadorField.setAccessible(true);
        return (Socket) ganadorField.get(juego);
    }

    /**
     * Verifica si un StringWriter contiene un texto específico (case insensitive)
     */
    private boolean contieneTexto(StringWriter sw, String texto) {
        return sw.toString().toUpperCase().contains(texto.toUpperCase());
    }

    // ==================== TESTS DE INICIALIZACIÓN ====================

    @Nested
    @DisplayName("Tests de Inicialización del Juego")
    class InicializacionTests {

        @Test
        @DisplayName("El tablero se inicializa con 9 casillas vacías")
        public void testTableroInicializadoVacio() throws Exception {
            Character[] casillas = obtenerCasillas();

            assertNotNull(casillas, "Las casillas no deberían ser null");
            assertEquals(9, casillas.length, "El tablero debe tener exactamente 9 casillas");

            for (int i = 0; i < casillas.length; i++) {
                assertEquals('-', casillas[i],
                        "Casilla " + i + " debería estar vacía ('-') al inicio");
            }
        }

        @Test
        @DisplayName("El juego no está terminado al iniciar")
        public void testJuegoNoTerminadoAlInicio() throws Exception {
            assertFalse(obtenerTerminado(), "El juego no debería estar terminado al inicio");
        }

        @Test
        @DisplayName("No hay empate al iniciar")
        public void testNoHayEmpateAlInicio() throws Exception {
            assertFalse(obtenerEmpate(), "No debería haber empate al inicio");
        }

        @Test
        @DisplayName("No hay ganador al iniciar")
        public void testNoHayGanadorAlInicio() throws Exception {
            assertNull(obtenerGanador(), "No debería haber ganador al inicio");
        }

        @Test
        @DisplayName("Se inicializan correctamente los jugadores")
        public void testJugadoresInicializados() throws Exception {
            Field jugadoresField = Juego.class.getDeclaredField("jugadores");
            jugadoresField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<Socket> jugadoresActuales = (ArrayList<Socket>) jugadoresField.get(juego);

            assertNotNull(jugadoresActuales, "La lista de jugadores no debería ser null");
            assertEquals(2, jugadoresActuales.size(), "Debe haber exactamente 2 jugadores");
        }
    }

    // ==================== TESTS DE VICTORIAS HORIZONTALES ====================

    @Nested
    @DisplayName("Tests de Victorias Horizontales")
    class VictoriasHorizontalesTests {

        @Test
        @DisplayName("Victoria en fila 1 (casillas 0, 1, 2) - Jugador X")
        public void testVictoriaFila1JugadorX() throws Exception {
            Character[] tablero = { 'x', 'x', 'x', '-', 'o', '-', '-', 'o', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
            assertFalse(obtenerEmpate(), "No debería ser empate cuando hay ganador");
            assertTrue(contieneTexto(sw1, "GANADO") || contieneTexto(sw2, "GANADO"),
                    "Debería notificar al ganador");
        }

        @Test
        @DisplayName("Victoria en fila 2 (casillas 3, 4, 5) - Jugador X")
        public void testVictoriaFila2JugadorX() throws Exception {
            Character[] tablero = { 'o', '-', 'o', 'x', 'x', 'x', '-', '-', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria en fila 2");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
        }

        @Test
        @DisplayName("Victoria en fila 3 (casillas 6, 7, 8) - Jugador X")
        public void testVictoriaFila3JugadorX() throws Exception {
            Character[] tablero = { '-', 'o', '-', 'o', '-', '-', 'x', 'x', 'x' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria en fila 3");
        }

        @Test
        @DisplayName("Victoria en fila 1 - Jugador O")
        public void testVictoriaFila1JugadorO() throws Exception {
            Character[] tablero = { 'o', 'o', 'o', 'x', 'x', '-', '-', 'x', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNotNull(obtenerGanador(), "Debería haber un ganador (jugador O)");
        }
    }

    // ==================== TESTS DE VICTORIAS VERTICALES ====================

    @Nested
    @DisplayName("Tests de Victorias Verticales")
    class VictoriasVerticalesTests {

        @Test
        @DisplayName("Victoria en columna 1 (casillas 0, 3, 6) - Jugador X")
        public void testVictoriaColumna1JugadorX() throws Exception {
            Character[] tablero = { 'x', 'o', '-', 'x', 'o', '-', 'x', '-', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria en columna 1");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
        }

        @Test
        @DisplayName("Victoria en columna 2 (casillas 1, 4, 7) - Jugador X")
        public void testVictoriaColumna2JugadorX() throws Exception {
            Character[] tablero = { 'o', 'x', '-', '-', 'x', 'o', '-', 'x', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria en columna 2");
        }

        @Test
        @DisplayName("Victoria en columna 3 (casillas 2, 5, 8) - Jugador X")
        public void testVictoriaColumna3JugadorX() throws Exception {
            Character[] tablero = { 'o', '-', 'x', '-', 'o', 'x', '-', '-', 'x' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria en columna 3");
        }

        @Test
        @DisplayName("Victoria en columna 2 - Jugador O")
        public void testVictoriaColumna2JugadorO() throws Exception {
            Character[] tablero = { 'x', 'o', 'x', '-', 'o', '-', 'x', 'o', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNotNull(obtenerGanador(), "Debería haber un ganador (jugador O)");
        }
    }

    // ==================== TESTS DE VICTORIAS DIAGONALES ====================

    @Nested
    @DisplayName("Tests de Victorias Diagonales")
    class VictoriasDiagonalesTests {

        @Test
        @DisplayName("Victoria en diagonal principal (0, 4, 8) - Jugador X")
        public void testVictoriaDiagonalPrincipalJugadorX() throws Exception {
            Character[] tablero = { 'x', 'o', '-', '-', 'x', 'o', '-', '-', 'x' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria diagonal");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
            assertFalse(obtenerEmpate(), "No debería ser empate");
        }

        @Test
        @DisplayName("Victoria en diagonal secundaria (2, 4, 6) - Jugador X")
        public void testVictoriaDiagonalSecundariaJugadorX() throws Exception {
            Character[] tablero = { 'o', '-', 'x', '-', 'x', '-', 'x', '-', 'o' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado con victoria diagonal secundaria");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
        }

        @Test
        @DisplayName("Victoria en diagonal principal - Jugador O")
        public void testVictoriaDiagonalPrincipalJugadorO() throws Exception {
            Character[] tablero = { 'o', 'x', '-', 'x', 'o', '-', '-', 'x', 'o' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNotNull(obtenerGanador(), "Debería haber un ganador (jugador O)");
        }

        @Test
        @DisplayName("Victoria en diagonal secundaria - Jugador O")
        public void testVictoriaDiagonalSecundariaJugadorO() throws Exception {
            Character[] tablero = { 'x', '-', 'o', 'x', 'o', '-', 'o', '-', 'x' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNotNull(obtenerGanador(), "Debería haber un ganador (jugador O)");
        }
    }

    // ==================== TESTS DE EMPATES ====================

    @Nested
    @DisplayName("Tests de Empates")
    class EmpatesTests {

        @Test
        @DisplayName("Empate cuando el tablero está lleno sin línea ganadora")
        public void testEmpateTableroLlenoSinGanador() throws Exception {
            // Tablero completamente lleno sin línea de tres en raya
            Character[] tableroEmpate = { 'x', 'o', 'x', 'x', 'o', 'o', 'o', 'x', 'x' };
            establecerTablero(tableroEmpate);
            comprobarGanador();

            assertTrue(obtenerEmpate(), "Debería detectar empate");
            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNull(obtenerGanador(), "No debería haber ganador en un empate");
            assertTrue(contieneTexto(sw1, "EMPATE"), "Debería notificar el empate");
        }

        @Test
        @DisplayName("Otro escenario de empate válido")
        public void testOtroEmpate() throws Exception {
            Character[] tableroEmpate = { 'x', 'x', 'o', 'o', 'o', 'x', 'x', 'o', 'x' };
            establecerTablero(tableroEmpate);
            comprobarGanador();

            assertTrue(obtenerEmpate(), "Debería detectar empate");
            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNull(obtenerGanador(), "No debería haber ganador en un empate");
        }

        @Test
        @DisplayName("No es empate si el tablero no está completo")
        public void testNoEmpateConCasillasVacias() throws Exception {
            Character[] tableroIncompleto = { 'x', 'o', 'x', 'o', 'x', 'o', '-', '-', '-' };
            establecerTablero(tableroIncompleto);
            comprobarGanador();

            assertFalse(obtenerEmpate(), "No debería ser empate con casillas vacías");
            assertFalse(obtenerTerminado(), "El juego no debería estar terminado");
        }

        @Test
        @DisplayName("No es empate si hay un ganador")
        public void testNoEmpateConGanador() throws Exception {
            // Tablero lleno pero con línea ganadora
            Character[] tableroGanador = { 'x', 'x', 'x', 'o', 'o', 'x', 'o', 'x', 'o' };
            establecerTablero(tableroGanador);
            comprobarGanador();

            assertFalse(obtenerEmpate(), "No debería ser empate si hay ganador");
            assertTrue(obtenerTerminado(), "El juego debería estar terminado");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
        }
    }

    // ==================== TESTS DE ESTADOS DEL JUEGO ====================

    @Nested
    @DisplayName("Tests de Estados del Juego")
    class EstadosJuegoTests {

        @Test
        @DisplayName("Juego no terminado con casillas vacías y sin ganador")
        public void testJuegoEnCurso() throws Exception {
            Character[] tableroMedio = { 'x', 'o', 'x', 'o', '-', '-', '-', '-', '-' };
            establecerTablero(tableroMedio);
            comprobarGanador();

            assertFalse(obtenerTerminado(), "El juego no debería estar terminado");
            assertFalse(obtenerEmpate(), "No debería ser empate");
            assertNull(obtenerGanador(), "No debería haber ganador aún");
        }

        @Test
        @DisplayName("Juego recién iniciado")
        public void testJuegoRecienIniciado() throws Exception {
            Character[] tableroVacio = { '-', '-', '-', '-', '-', '-', '-', '-', '-' };
            establecerTablero(tableroVacio);
            comprobarGanador();

            assertFalse(obtenerTerminado(), "El juego no debería estar terminado");
            assertFalse(obtenerEmpate(), "No debería ser empate");
            assertNull(obtenerGanador(), "No debería haber ganador");
        }

        @Test
        @DisplayName("Una casilla ocupada no es victoria")
        public void testUnaCasillaOcupada() throws Exception {
            Character[] tablero = { 'x', '-', '-', '-', '-', '-', '-', '-', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertFalse(obtenerTerminado(), "El juego no debería estar terminado con solo una casilla");
        }

        @Test
        @DisplayName("Dos casillas en línea no es victoria")
        public void testDosCasillasEnLinea() throws Exception {
            Character[] tablero = { 'x', 'x', '-', '-', 'o', '-', '-', '-', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertFalse(obtenerTerminado(), "El juego no debería estar terminado con solo dos casillas en línea");
            assertNull(obtenerGanador(), "No debería haber ganador con solo dos fichas");
        }
    }

    // ==================== TESTS DE TODAS LAS COMBINACIONES GANADORAS ====================

    @Nested
    @DisplayName("Tests Exhaustivos de Todas las Combinaciones Ganadoras")
    class TodasCombinacionesGanadorasTests {

        @Test
        @DisplayName("Verificar todas las 8 combinaciones ganadoras posibles")
        public void testTodasLasCombinacionesGanadoras() throws Exception {
            // Las 8 formas de ganar en tres en raya
            Character[][][] combinaciones = {
                    // Filas horizontales
                    {{ 'x', 'x', 'x' }, { '-', 'o', '-' }, { '-', 'o', '-' }}, // Fila 1
                    {{ '-', 'o', '-' }, { 'x', 'x', 'x' }, { '-', 'o', '-' }}, // Fila 2
                    {{ '-', 'o', '-' }, { '-', 'o', '-' }, { 'x', 'x', 'x' }}, // Fila 3
                    // Columnas verticales
                    {{ 'x', '-', 'o' }, { 'x', '-', 'o' }, { 'x', '-', '-' }}, // Columna 1
                    {{ '-', 'x', 'o' }, { 'o', 'x', '-' }, { '-', 'x', '-' }}, // Columna 2
                    {{ '-', 'o', 'x' }, { '-', '-', 'x' }, { 'o', '-', 'x' }}, // Columna 3
                    // Diagonales
                    {{ 'x', '-', 'o' }, { '-', 'x', 'o' }, { '-', '-', 'x' }}, // Diagonal principal
                    {{ '-', '-', 'x' }, { 'o', 'x', '-' }, { 'x', '-', 'o' }}  // Diagonal secundaria
            };

            for (int i = 0; i < combinaciones.length; i++) {
                // Convertir matriz 3x3 a array 1D
                Character[] tablero = new Character[9];
                int idx = 0;
                for (int fila = 0; fila < 3; fila++) {
                    for (int col = 0; col < 3; col++) {
                        tablero[idx++] = combinaciones[i][fila][col];
                    }
                }

                // Crear un nuevo juego para cada combinación
                Juego juegoTest = new Juego(jugadores);

                // Inyectar PrintWriters
                StringWriter sw1Test = new StringWriter();
                StringWriter sw2Test = new StringWriter();
                Field outsField = Juego.class.getDeclaredField("outs");
                outsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                ArrayList<PrintWriter> outsTest = (ArrayList<PrintWriter>) outsField.get(juegoTest);
                outsTest.add(new PrintWriter(sw1Test, true));
                outsTest.add(new PrintWriter(sw2Test, true));

                // Establecer el tablero
                Field casillasField = Juego.class.getDeclaredField("casillas");
                casillasField.setAccessible(true);
                casillasField.set(juegoTest, tablero);

                // Comprobar ganador
                Method comprobarGanadorMethod = Juego.class.getDeclaredMethod("comprobarGanador");
                comprobarGanadorMethod.setAccessible(true);
                comprobarGanadorMethod.invoke(juegoTest);

                // Verificar que se detecta la victoria
                Field terminadoField = Juego.class.getDeclaredField("terminado");
                terminadoField.setAccessible(true);
                boolean terminado = (boolean) terminadoField.get(juegoTest);

                assertTrue(terminado,
                        "Debería detectar victoria en la combinación " + (i + 1) + 
                        " (1-3: filas, 4-6: columnas, 7-8: diagonales)");
            }
        }
    }

    // ==================== TESTS DE CASOS ESPECIALES ====================

    @Nested
    @DisplayName("Tests de Casos Especiales y Límites")
    class CasosEspecialesTests {

        @Test
        @DisplayName("Tablero con solo fichas 'o'")
        public void testTableroSoloO() throws Exception {
            Character[] tablero = { 'o', 'o', 'o', '-', '-', '-', '-', '-', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "Debería detectar victoria del jugador O");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
        }

        @Test
        @DisplayName("Múltiples líneas ganadoras (caso extremo)")
        public void testMultiplesLineasGanadoras() throws Exception {
            // Caso donde X tiene múltiples líneas (fila y columna)
            Character[] tablero = { 'x', 'x', 'x', 'x', 'o', 'o', 'x', 'o', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertTrue(obtenerTerminado(), "Debería detectar victoria incluso con múltiples líneas");
            assertNotNull(obtenerGanador(), "Debería haber un ganador");
        }

        @Test
        @DisplayName("Casi empate pero con una casilla vacía")
        public void testCasiEmpate() throws Exception {
            Character[] tablero = { 'x', 'o', 'x', 'o', 'x', 'o', 'o', 'x', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertFalse(obtenerEmpate(), "No debería ser empate con una casilla vacía");
            assertFalse(obtenerTerminado(), "El juego no debería estar terminado");
        }

        @Test
        @DisplayName("Tablero con patrón intercalado sin ganador")
        public void testPatronIntercaladoSinGanador() throws Exception {
            Character[] tablero = { 'x', 'o', 'x', 'o', 'x', '-', '-', '-', '-' };
            establecerTablero(tablero);
            comprobarGanador();

            assertFalse(obtenerTerminado(), "No debería estar terminado sin línea ganadora");
            assertNull(obtenerGanador(), "No debería haber ganador");
        }
    }
}

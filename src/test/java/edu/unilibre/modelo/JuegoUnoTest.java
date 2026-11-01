package edu.unilibre.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JuegoUnoTest {

    private JuegoUno juego;
    private Jugador ana;
    private Jugador robot1;
    private Jugador robot2;
    private Jugador robot3;

    @BeforeEach
    void preparar() {
        juego = new JuegoUno();
        juego.agregarJugador("Ana");
        juego.agregarJugador("Robot 1");
        juego.agregarJugador("Robot 2");
        juego.agregarJugador("Robot 3");
        juego.iniciarJuego();
        List<Jugador> jugadores = juego.getJugadores();
        ana = jugadores.get(0);
        robot1 = jugadores.get(1);
        robot2 = jugadores.get(2);
        robot3 = jugadores.get(3);
    }

    private void darMano(Jugador jugador, Carta... cartas) {
        jugador.limpiarMano();
        for (Carta carta : cartas) {
            jugador.agregarCarta(carta);
        }
    }

    private String otroColor(String excluido) {
        return List.of("ROJO", "AMARILLO", "VERDE", "AZUL").stream()
                .filter(c -> !c.equals(excluido)).findFirst().orElseThrow();
    }

    private Carta delColorActual(String valor) {
        return new Carta(juego.getColorActual(), valor);
    }

    @Test
    void repartoInicialYEstadoDeLaMesa() {
        juego.getJugadores().forEach(j -> assertEquals(7, j.cantidadCartas()));
        assertSame(ana, juego.getJugadorActual());
        assertEquals(1, juego.getNumeroRonda());
        assertEquals(1, juego.cartasEnPila());
        assertEquals(79, juego.cartasEnMazo());
        int enManos = juego.getJugadores().stream().mapToInt(Jugador::cantidadCartas).sum();
        assertEquals(108, enManos + juego.cartasEnMazo() + juego.cartasEnPila());
        assertEquals(juego.getCartaSuperior().obtenerColor(), juego.getColorActual());

        for (int i = 0; i < 100; i++) {
            JuegoUno otro = new JuegoUno();
            otro.agregarJugador("A");
            otro.agregarJugador("B");
            otro.iniciarJuego();
            assertFalse(otro.getCartaSuperior().esEspecial());
        }

        JuegoUno solo = new JuegoUno();
        solo.agregarJugador("Ana");
        assertThrows(IllegalStateException.class, solo::iniciarJuego);
        assertThrows(IllegalStateException.class, () -> juego.agregarJugador("Otro"));
        assertThrows(UnsupportedOperationException.class, () -> juego.getJugadores().add(new Jugador("X")));
    }

    @Test
    void jugadaValidaInvalidaYRobar() {
        Carta valida = delColorActual("7");
        darMano(ana, valida, delColorActual("1"), delColorActual("2"));
        juego.jugarCarta(valida);
        assertEquals(2, ana.cantidadCartas());
        assertSame(valida, juego.getCartaSuperior());
        assertSame(robot1, juego.getJugadorActual());

        Carta invalida = new Carta(otroColor(juego.getColorActual()), "SALTO");
        darMano(robot1, invalida, delColorActual("1"), delColorActual("2"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juego.jugarCarta(invalida));
        assertTrue(ex.getMessage().contains("no válida"));
        assertEquals(3, robot1.cantidadCartas());
        assertSame(robot1, juego.getJugadorActual());
        assertThrows(IllegalArgumentException.class, () -> juego.jugarCarta(delColorActual("9")));

        assertNotNull(juego.robarCarta());
        assertEquals(4, robot1.cantidadCartas());
        assertSame(robot2, juego.getJugadorActual());
    }

    @Test
    void saltaYReversaCambianElTurno() {
        Carta salto = delColorActual("SALTO");
        darMano(ana, salto, delColorActual("1"), delColorActual("2"));
        juego.jugarCarta(salto);
        assertSame(robot2, juego.getJugadorActual());

        Carta reversa = delColorActual("REVERSA");
        darMano(robot2, reversa, delColorActual("1"), delColorActual("2"));
        juego.jugarCarta(reversa);
        assertSame(robot1, juego.getJugadorActual());

        JuegoUno dos = new JuegoUno();
        dos.agregarJugador("A");
        dos.agregarJugador("B");
        dos.iniciarJuego();
        Jugador a = dos.getJugadores().get(0);
        Carta reversaDeDos = new Carta(dos.getColorActual(), "REVERSA");
        darMano(a, reversaDeDos, new Carta(dos.getColorActual(), "1"), new Carta(dos.getColorActual(), "2"));
        dos.jugarCarta(reversaDeDos);
        assertSame(a, dos.getJugadorActual());
    }

    @Test
    void masDosSeAcumulaYSePuedeContrarrestar() {
        Carta masDos = delColorActual("MAS_DOS");
        darMano(ana, masDos, delColorActual("1"), delColorActual("2"));
        juego.jugarCarta(masDos);
        assertEquals(2, juego.getPenalizacionAcumulada());
        assertSame(robot1, juego.getJugadorActual());

        Carta numero = delColorActual("3");
        Carta respuesta = new Carta("ROJO", "MAS_DOS");
        darMano(robot1, respuesta, numero, delColorActual("4"));
        assertTrue(juego.puedeContrarrestarPenalizacion());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juego.jugarCarta(numero));
        assertTrue(ex.getMessage().contains("+2"));

        juego.jugarCarta(respuesta);
        assertEquals(4, juego.getPenalizacionAcumulada());
        assertSame(robot2, juego.getJugadorActual());

        darMano(robot2, delColorActual("5"), delColorActual("6"), delColorActual("7"));
        assertFalse(juego.puedeContrarrestarPenalizacion());
        juego.robarCarta();
        assertEquals(7, robot2.cantidadCartas());
        assertEquals(0, juego.getPenalizacionAcumulada());
        assertSame(robot3, juego.getJugadorActual());
    }

    @Test
    void masCuatroYCambioDeColor() {
        Carta masCuatro = new Carta("CAMBIO_COLOR", "MAS_CUATRO");
        darMano(ana, masCuatro, delColorActual("1"), delColorActual("2"));
        assertThrows(IllegalArgumentException.class, () -> juego.jugarCarta(masCuatro));
        assertEquals(3, ana.cantidadCartas());

        String otro = otroColor(juego.getColorActual());
        darMano(ana, masCuatro, new Carta(otro, "1"), new Carta(otro, "2"));
        juego.jugarCarta(masCuatro);
        assertEquals(4, juego.getPenalizacionAcumulada());
        assertTrue(juego.hayColorPendiente());
        assertSame(ana, juego.getJugadorActual());
        assertThrows(IllegalStateException.class, () -> juego.robarCarta());
        assertThrows(IllegalArgumentException.class, () -> juego.cambiarColor("MORADO"));
        assertThrows(IllegalArgumentException.class, () -> juego.cambiarColor("CAMBIO_COLOR"));
        juego.cambiarColor(otro);
        assertEquals(otro, juego.getColorActual());
        assertFalse(juego.hayColorPendiente());
        assertSame(robot1, juego.getJugadorActual());

        juego.robarCarta();
        assertSame(robot2, juego.getJugadorActual());
        Carta comodin = new Carta("CAMBIO_COLOR", "CAMBIO_COLOR");
        darMano(robot2, comodin, delColorActual("1"), delColorActual("2"));
        juego.jugarCarta(comodin);
        assertTrue(juego.hayColorPendiente());
        juego.cambiarColor("AZUL");
        assertEquals("AZUL", juego.getColorActual());
        assertSame(robot3, juego.getJugadorActual());
    }

    @Test
    void reglaDelUno() {
        Carta primera = delColorActual("1");
        darMano(ana, primera, delColorActual("2"));
        juego.jugarCarta(primera);
        assertTrue(juego.hayUnoPendiente());
        assertSame(ana, juego.getJugadorQueDebeDecirUno());

        juego.robarCarta();
        assertTrue(juego.huboPenalizacionUno());
        assertSame(ana, juego.getJugadorPenalizadoUno());
        assertEquals(3, ana.cantidadCartas());
        assertFalse(juego.hayUnoPendiente());

        Carta segunda = delColorActual("3");
        darMano(robot2, segunda, delColorActual("4"));
        juego.jugarCarta(segunda);
        juego.decirUno();
        assertFalse(juego.hayUnoPendiente());
        juego.robarCarta();
        assertFalse(juego.huboPenalizacionUno());
        assertEquals(1, robot2.cantidadCartas());
        assertThrows(IllegalStateException.class, () -> juego.decirUno());
    }

    @Test
    void finDeRondaYDePartida() {
        Carta ultima = delColorActual("3");
        darMano(ana, ultima);
        int esperados = robot1.calcularPuntosMano() + robot2.calcularPuntosMano() + robot3.calcularPuntosMano();
        juego.jugarCarta(ultima);
        assertTrue(juego.hayGanador());
        assertSame(ana, juego.getGanador());
        assertEquals(esperados, juego.getPuntosGanadorRonda());
        assertEquals(esperados, ana.obtenerPuntos());
        assertEquals(esperados >= 250, juego.getGanadorPartida() == ana);
        assertThrows(IllegalStateException.class, () -> juego.robarCarta());

        if (juego.getGanadorPartida() == null) {
            juego.nuevaRonda();
            assertEquals(2, juego.getNumeroRonda());
            assertEquals(esperados, ana.obtenerPuntos());
            juego.getJugadores().forEach(j -> assertEquals(7, j.cantidadCartas()));
            assertThrows(IllegalStateException.class, () -> juego.nuevaRonda());
        }

        JuegoUno otra = new JuegoUno();
        otra.agregarJugador("A");
        otra.agregarJugador("B");
        otra.iniciarJuego();
        Jugador a = otra.getJugadores().get(0);
        Jugador b = otra.getJugadores().get(1);
        a.sumarPuntos(240);
        b.agregarCarta(new Carta("CAMBIO_COLOR", "MAS_CUATRO"));
        Carta ultimaDeA = new Carta(otra.getColorActual(), "3");
        darMano(a, ultimaDeA);
        otra.jugarCarta(ultimaDeA);
        assertSame(a, otra.getGanadorPartida());
        assertTrue(a.obtenerPuntos() >= 250);
        assertThrows(IllegalStateException.class, otra::nuevaRonda);
    }

    @Test
    void cuandoSeAcabaElMazoSeRepongaDesdeLaPila() {
        Carta jugada = delColorActual("4");
        darMano(ana, jugada, delColorActual("1"), delColorActual("2"));
        juego.jugarCarta(jugada);
        assertEquals(2, juego.cartasEnPila());

        while (juego.cartasEnMazo() > 0) {
            juego.robarCarta();
        }
        assertNotNull(juego.robarCarta());
        assertSame(jugada, juego.getCartaSuperior());
        assertEquals(1, juego.cartasEnPila());
        assertEquals(0, juego.cartasEnMazo());

        assertNull(juego.robarCarta());
    }
}

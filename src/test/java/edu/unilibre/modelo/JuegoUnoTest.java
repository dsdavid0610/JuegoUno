package edu.unilibre.modelo;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}
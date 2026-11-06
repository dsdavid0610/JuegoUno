package edu.unilibre.modelo;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RobotEstandarTest {

    private JuegoUno juego;
    private Jugador ana;
    private Jugador robot;
    private RobotEstandar estrategia;

    @BeforeEach
    void preparar() {
        juego = new JuegoUno();
        juego.agregarJugador("Ana");
        juego.agregarJugador("Robot 1");
        juego.agregarJugador("Robot 2");
        juego.agregarJugador("Robot 3");
        juego.iniciarJuego();
        ana = juego.getJugadores().get(0);
        robot = juego.getJugadores().get(1);
        estrategia = new RobotEstandar();
    }

    private void darMano(Jugador jugador, Carta... cartas) {
        jugador.limpiarMano();
        for (Carta carta : cartas) {
            jugador.agregarCarta(carta);
        }
    }

    private String otroColor() {
        return List.of("ROJO", "AMARILLO", "VERDE", "AZUL").stream()
                .filter(c -> !c.equals(juego.getColorActual())).findFirst().orElseThrow();
    }

    @Test
    void soloJuegaCartasValidasYGuardaLosComodines() {
        darMano(robot, new Carta(otroColor(), "SALTO"), new Carta(otroColor(), "REVERSA"));
        assertTrue(estrategia.elegirCarta(juego, robot).isEmpty());

        Carta comodin = new Carta("CAMBIO_COLOR", "CAMBIO_COLOR");
        Carta normal = new Carta(juego.getColorActual(), "5");
        darMano(robot, comodin, normal);
        for (int i = 0; i < 30; i++) {
            assertSame(normal, estrategia.elegirCarta(juego, robot).orElseThrow());
        }

        darMano(robot, comodin, new Carta(otroColor(), "SALTO"));
        assertSame(comodin, estrategia.elegirCarta(juego, robot).orElseThrow());

        Carta masCuatro = new Carta("CAMBIO_COLOR", "MAS_CUATRO");
        Carta delColor = new Carta(juego.getColorActual(), "SALTO");
        darMano(robot, masCuatro, delColor);
        for (int i = 0; i < 30; i++) {
            assertSame(delColor, estrategia.elegirCarta(juego, robot).orElseThrow());
        }
        darMano(robot, masCuatro, new Carta(otroColor(), "SALTO"));
        assertSame(masCuatro, estrategia.elegirCarta(juego, robot).orElseThrow());
    }
}

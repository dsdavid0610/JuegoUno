package edu.unilibre.modelo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SimulacionPartidaTest {

    private JuegoUno crearPartida(int robots) {
        JuegoUno juego = new JuegoUno();
        juego.agregarJugador("Jugador");
        for (int i = 1; i <= robots; i++) {
            juego.agregarJugador("Robot " + i);
        }
        juego.iniciarJuego();
        return juego;
    }

    private void jugarTurno(JuegoUno juego, EstrategiaRobot estrategia) {
        Jugador actual = juego.getJugadorActual();
        Optional<Carta> eleccion = estrategia.elegirCarta(juego, actual);
        if (eleccion.isPresent()) {
            juego.jugarCarta(eleccion.get());
            if (juego.hayColorPendiente()) {
                juego.cambiarColor(estrategia.elegirColor(actual));
            }
            if (juego.hayUnoPendiente()) {
                juego.decirUno();
            }
        } else {
            juego.robarCarta();
        }
    }

    private void verificarTotalDeCartas(JuegoUno juego) {
        int enManos = juego.getJugadores().stream().mapToInt(Jugador::cantidadCartas).sum();
        assertEquals(108, enManos + juego.cartasEnMazo() + juego.cartasEnPila());
    }

    @Test
    void unJugadorContraTresRobotsJueganPartidasCompletas() {
        EstrategiaRobot estrategia = new RobotEstandar();
        int limitePasos = 50_000;
        for (int partida = 0; partida < 10; partida++) {
            JuegoUno juego = crearPartida(3);
            int pasos = 0;
            while (juego.getGanadorPartida() == null && pasos++ < limitePasos) {
                if (juego.hayGanador()) {
                    juego.nuevaRonda();
                    continue;
                }
                jugarTurno(juego, estrategia);
                verificarTotalDeCartas(juego);
            }
            assertTrue(pasos < limitePasos, "La partida no terminó");
            assertNotNull(juego.getGanadorPartida());
            assertTrue(juego.getGanadorPartida().obtenerPuntos() >= 250);
            long conPuntosAltos = juego.getJugadores().stream().filter(j -> j.obtenerPuntos() >= 250).count();
            assertEquals(1, conPuntosAltos);
        }

        for (int robots = 1; robots <= 2; robots++) {
            JuegoUno juego = crearPartida(robots);
            int pasos = 0;
            while (!juego.hayGanador() && pasos++ < limitePasos) {
                jugarTurno(juego, estrategia);
            }
            assertTrue(juego.hayGanador(), "Falló con " + robots + " robots");
            assertTrue(juego.getGanador().gano());
        }
    }
}

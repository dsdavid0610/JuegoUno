package edu.unilibre.servicio;

import edu.unilibre.dao.PartidaDAO;
import edu.unilibre.dao.PartidaDAOMemoria;
import edu.unilibre.dto.EstadisticasDTO;
import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.modelo.Carta;
import edu.unilibre.modelo.JuegoUno;
import edu.unilibre.modelo.Jugador;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioHistorialTest {

    private JuegoUno nuevoJuego() {
        JuegoUno juego = new JuegoUno();
        juego.agregarJugador("Ana");
        juego.agregarJugador("Robot 1");
        juego.agregarJugador("Robot 2");
        juego.agregarJugador("Robot 3");
        juego.iniciarJuego();
        return juego;
    }

    private void ganarPartida(JuegoUno juego, Jugador ganador) {
        ganador.sumarPuntos(240);
        juego.getJugadores().stream().filter(j -> j != ganador)
                .forEach(j -> j.agregarCarta(new Carta("CAMBIO_COLOR", "MAS_CUATRO")));
        Carta ultima = new Carta(juego.getColorActual(), "3");
        ganador.limpiarMano();
        ganador.agregarCarta(ultima);
        juego.jugarCarta(ultima);
    }

    @Test
    void registraPartidasCalculaEstadisticasYBorra() {
        assertThrows(IllegalArgumentException.class, () -> new ServicioHistorial(null));
        PartidaDAO dao = new PartidaDAOMemoria();
        ServicioHistorial servicio = new ServicioHistorial(dao);

        assertEquals(0, servicio.estadisticas().getPartidas());
        assertThrows(IllegalStateException.class, () -> servicio.registrarPartida(nuevoJuego()));

        JuegoUno ganaHumano = nuevoJuego();
        ganarPartida(ganaHumano, ganaHumano.getJugadores().get(0));
        PartidaDTO primera = servicio.registrarPartida(ganaHumano);
        assertEquals(1, primera.getId());
        assertEquals("Ana", primera.getNombreGanador());
        assertTrue(primera.isGanoHumano());
        assertTrue(primera.getPuntosGanador() >= 250);

        JuegoUno ganaRobot = nuevoJuego();
        ganaRobot.robarCarta();
        ganarPartida(ganaRobot, ganaRobot.getJugadores().get(1));
        PartidaDTO segunda = servicio.registrarPartida(ganaRobot);
        assertEquals(2, segunda.getId());
        assertEquals("Robot 1", segunda.getNombreGanador());
        assertFalse(segunda.isGanoHumano());

        assertEquals(2, servicio.listar().size());
        EstadisticasDTO e = servicio.estadisticas();
        assertEquals(2, e.getPartidas());
        assertEquals(1, e.getVictorias());
        assertEquals(1, e.getDerrotas());
        assertEquals(1.0, e.getRondasPromedio(), 0.0001);

        assertTrue(servicio.borrar(1));
        assertFalse(servicio.borrar(1));
        servicio.limpiar();
        assertTrue(servicio.listar().isEmpty());
    }
}

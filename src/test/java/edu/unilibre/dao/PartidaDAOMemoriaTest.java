package edu.unilibre.dao;

import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.servicio.ServicioHistorial;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PartidaDAOMemoriaTest {

    private PartidaDTO partida(int id, String humano) {
        return PartidaDTO.crear(id, LocalDate.of(2026, 10, 27), humano, "Robot 1", false, 260, 4);
    }

    @Test
    void guardaBuscaActualizaYElimina() {
        PartidaDAOMemoria dao = new PartidaDAOMemoria();
        assertEquals(1, dao.siguienteId());

        dao.guardar(partida(1, "Ana"));
        dao.guardar(partida(2, "Luis"));
        assertEquals(3, dao.siguienteId());
        assertThrows(IllegalArgumentException.class, () -> dao.guardar(partida(1, "Otra")));
        assertThrows(IllegalArgumentException.class, () -> dao.guardar(null));
        assertEquals("Ana", dao.buscarPorId(1).orElseThrow().getNombreHumano());
        assertTrue(dao.buscarPorId(99).isEmpty());

        dao.listarTodos().clear();
        assertEquals(2, dao.listarTodos().size());
        assertEquals(1, dao.buscarPorJugador("ANA").size());

        dao.actualizar(partida(2, "Luisa"));
        assertEquals("Luisa", dao.buscarPorId(2).orElseThrow().getNombreHumano());
        assertThrows(IllegalArgumentException.class, () -> dao.actualizar(partida(9, "Nadie")));

        assertTrue(dao.eliminar(1));
        assertFalse(dao.eliminar(1));
        assertEquals(1, dao.listarTodos().size());

        PartidaDAOMemoria otro = new PartidaDAOMemoria();
        otro.guardar(partida(1, "Ana"));
        otro.guardar(partida(2, "Luisa"));
        dao.operaciones(new ServicioHistorial(otro));
        assertEquals(2, dao.listarTodos().size());
    }
}

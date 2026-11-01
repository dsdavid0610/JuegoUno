package edu.unilibre.dao;

import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.servicio.ServicioHistorial;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PartidaDAOArchivoTest {

    @TempDir
    Path carpeta;

    private PartidaDTO partida(int id, String humano) {
        return PartidaDTO.crear(id, LocalDate.of(2026, 10, 27), humano, "Robot 1", false, 270, 6);
    }

    @Test
    void guardaEnArchivoYRecuperaAlReabrir() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> new PartidaDAOArchivo(null));
        assertTrue(new PartidaDAOArchivo(carpeta.resolve("no_existe.csv")).listarTodos().isEmpty());

        Path archivo = carpeta.resolve("a").resolve("historial.csv");
        PartidaDAOArchivo dao = new PartidaDAOArchivo(archivo);
        dao.guardar(partida(1, "Ana"));
        dao.guardar(partida(2, "Luis"));
        dao.guardar(partida(3, "Ana;Maria"));
        assertTrue(Files.exists(archivo));

        PartidaDAOArchivo reabierto = new PartidaDAOArchivo(archivo);
        assertEquals(3, reabierto.listarTodos().size());
        PartidaDTO leida = reabierto.buscarPorId(1).orElseThrow();
        assertEquals(LocalDate.of(2026, 10, 27), leida.getFecha());
        assertEquals(270, leida.getPuntosGanador());
        assertEquals(6, leida.getRondas());
        assertEquals("Ana,Maria", reabierto.buscarPorId(3).orElseThrow().getNombreHumano());
        assertEquals(4, reabierto.siguienteId());

        assertTrue(dao.eliminar(1));
        assertFalse(dao.eliminar(1));
        dao.actualizar(partida(2, "Luisa"));
        PartidaDAOArchivo despues = new PartidaDAOArchivo(archivo);
        assertEquals(2, despues.listarTodos().size());
        assertEquals("Luisa", despues.buscarPorId(2).orElseThrow().getNombreHumano());
        assertEquals(1, despues.buscarPorJugador("luisa").size());

        Files.write(archivo, List.of(
                "1;2026-10-27;Ana;Robot 1;false;270;6",
                "esto no es una partida",
                "",
                "x;fecha;mala;linea;true;1;2"));
        dao.cargar();
        assertEquals(1, dao.listarTodos().size());

        PartidaDAOMemoria origen = new PartidaDAOMemoria();
        origen.guardar(partida(1, "Ana"));
        origen.guardar(partida(5, "Pedro"));
        PartidaDAOArchivo destino = new PartidaDAOArchivo(carpeta.resolve("copia.csv"));
        destino.operaciones(new ServicioHistorial(origen));
        assertEquals(2, new PartidaDAOArchivo(carpeta.resolve("copia.csv")).listarTodos().size());
    }
}

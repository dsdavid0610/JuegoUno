package edu.unilibre.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void losDTOTransportanSusDatos() {
        LocalDate hoy = LocalDate.of(2026, 10, 27);
        PartidaDTO p = PartidaDTO.crear(3, hoy, "Ana", "Robot 2", false, 265, 5);
        assertEquals(3, p.getId());
        assertEquals(hoy, p.getFecha());
        assertEquals("Ana", p.getNombreHumano());
        assertEquals("Robot 2", p.getNombreGanador());
        assertFalse(p.isGanoHumano());
        assertEquals(265, p.getPuntosGanador());
        assertEquals(5, p.getRondas());
        assertTrue(p.toString().contains("#3"));

        EstadisticasDTO e = EstadisticasDTO.crear(10, 4, 6, 3.5);
        assertEquals(10, e.getPartidas());
        assertEquals(4, e.getVictorias());
        assertEquals(6, e.getDerrotas());
        assertEquals(3.5, e.getRondasPromedio(), 0.0001);
        assertFalse(e.eliminar(1));
    }
}

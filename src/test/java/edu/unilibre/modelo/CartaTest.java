package edu.unilibre.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartaTest {

    @Test
    void validaClasificaYPuntuaLasCartas() {
        assertEquals("ROJO", new Carta("ROJO", "5").obtenerColor());
        assertEquals("5", new Carta("ROJO", "5").obtenerValor());

        assertThrows(IllegalArgumentException.class, () -> new Carta("MORADO", "5"));
        assertThrows(IllegalArgumentException.class, () -> new Carta("ROJO", "10"));
        assertThrows(IllegalArgumentException.class, () -> new Carta("ROJO", "MAS_CUATRO"));
        assertThrows(IllegalArgumentException.class, () -> new Carta("CAMBIO_COLOR", "5"));
        assertThrows(IllegalArgumentException.class, () -> new Carta(null, "5"));

        assertTrue(new Carta("AZUL", "SALTO").esSalta());
        assertTrue(new Carta("AZUL", "REVERSA").esReversa());
        assertTrue(new Carta("AZUL", "MAS_DOS").esMasDos());
        assertTrue(new Carta("CAMBIO_COLOR", "MAS_CUATRO").esMasCuatro());
        assertTrue(new Carta("CAMBIO_COLOR", "CAMBIO_COLOR").esCambioColor());
        assertTrue(new Carta("CAMBIO_COLOR", "MAS_CUATRO").esComodin());
        assertTrue(new Carta("AZUL", "MAS_DOS").esEspecial());
        assertFalse(new Carta("AZUL", "7").esEspecial());

        assertEquals(0, new Carta("VERDE", "7").puntos());
        assertEquals(0, new Carta("VERDE", "0").puntos());
        assertEquals(20, new Carta("VERDE", "SALTO").puntos());
        assertEquals(20, new Carta("VERDE", "REVERSA").puntos());
        assertEquals(20, new Carta("VERDE", "MAS_DOS").puntos());
        assertEquals(20, new Carta("CAMBIO_COLOR", "MAS_CUATRO").puntos());
        assertEquals(50, new Carta("CAMBIO_COLOR", "CAMBIO_COLOR").puntos());
    }

    @Test
    void sabeCuandoSePuedeJugarSobreOtraCarta() {
        Carta mesa = new Carta("ROJO", "5");
        assertTrue(new Carta("ROJO", "9").sePuedeJugarSobre(mesa));
        assertTrue(new Carta("AZUL", "5").sePuedeJugarSobre(mesa));
        assertFalse(new Carta("AZUL", "9").sePuedeJugarSobre(mesa));
        assertTrue(new Carta("CAMBIO_COLOR", "CAMBIO_COLOR").sePuedeJugarSobre(mesa));
        assertTrue(new Carta("CAMBIO_COLOR", "MAS_CUATRO").sePuedeJugarSobre(mesa));

        Carta comodinEnMesa = new Carta("CAMBIO_COLOR", "CAMBIO_COLOR");
        assertTrue(new Carta("AZUL", "3").sePuedeJugarSobre(comodinEnMesa, "AZUL"));
        assertFalse(new Carta("ROJO", "3").sePuedeJugarSobre(comodinEnMesa, "AZUL"));

        assertThrows(IllegalArgumentException.class, () -> new Carta("ROJO", "1").sePuedeJugarSobre(null));

        assertEquals("ROJO 5", new Carta("ROJO", "5").toString());
        assertEquals("ROJO +2", new Carta("ROJO", "MAS_DOS").toString());
        assertEquals("COMODIN +4", new Carta("CAMBIO_COLOR", "MAS_CUATRO").toString());
    }
}

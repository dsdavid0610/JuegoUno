package edu.unilibre.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JugadorTest {

    @Test
    void manejaSuManoYSusPuntos() {
        assertThrows(IllegalArgumentException.class, () -> new Jugador(" "));
        Jugador jugador = new Jugador("Ana");
        assertEquals("Ana", jugador.obtenerNombre());
        assertTrue(jugador.gano());

        Carta a = new Carta("ROJO", "1");
        Carta b = new Carta("AZUL", "2");
        jugador.agregarCarta(a);
        jugador.recibirCarta(b);
        assertEquals(2, jugador.cantidadCartas());
        assertTrue(jugador.tieneCarta(a));
        assertFalse(jugador.gano());
        assertThrows(UnsupportedOperationException.class, () -> jugador.obtenerMano().add(a));

        jugador.eliminarCarta(a);
        assertFalse(jugador.tieneCarta(a));
        assertThrows(IllegalArgumentException.class, () -> jugador.eliminarCarta(a));
        jugador.limpiarMano();
        assertEquals(0, jugador.cantidadCartas());

        jugador.sumarPuntos(30);
        jugador.sumarPuntos(20);
        assertEquals(50, jugador.obtenerPuntos());
        assertThrows(IllegalArgumentException.class, () -> jugador.sumarPuntos(-1));
        jugador.reiniciarPuntos();
        assertEquals(0, jugador.obtenerPuntos());
    }

    @Test
    void sabeQueCartasPuedeJugarYCuantoVaLaMano() {
        Jugador jugador = new Jugador("Ana");
        jugador.agregarCarta(new Carta("AZUL", "9"));
        jugador.agregarCarta(new Carta("AZUL", "SALTO"));
        jugador.agregarCarta(new Carta("CAMBIO_COLOR", "CAMBIO_COLOR"));

        assertTrue(jugador.puedeJugar(new Carta("ROJO", "SALTO")));
        Carta comodinEnMesa = new Carta("CAMBIO_COLOR", "CAMBIO_COLOR");
        assertTrue(jugador.puedeJugar(comodinEnMesa, "AZUL"));
        assertTrue(jugador.tieneCartaDeColor("AZUL"));
        assertFalse(jugador.tieneCartaDeColor("ROJO"));
        assertEquals(0 + 20 + 50, jugador.calcularPuntosMano());
        assertTrue(jugador.toString().contains("Ana"));
    }
}

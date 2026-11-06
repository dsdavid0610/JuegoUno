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
}
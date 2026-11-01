package edu.unilibre.vista;

import edu.unilibre.modelo.Carta;
import edu.unilibre.modelo.JuegoUno;
import edu.unilibre.modelo.Jugador;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class TableroPanelTest {

    private static class AccionesFalsas implements AccionesTableroPanel {
        Carta jugada;
        int robadas;
        int unos;
        int menus;

        @Override
        public void jugar(Carta carta) {
            jugada = carta;
        }

        @Override
        public void robar() {
            robadas++;
        }

        @Override
        public void decirUno() {
            unos++;
        }

        @Override
        public void botonFin() {
        }

        @Override
        public void volverMenu() {
            menus++;
        }
    }

    private void clic(TableroPanel tablero, int x, int y) {
        tablero.getMouseListeners()[0].mouseClicked(
                new MouseEvent(tablero, MouseEvent.MOUSE_CLICKED, 0, 0, x, y, 1, false));
    }

    @Test
    void detectaElTurnoLosClicsLosBotonesYSePintaTodo() {
        JuegoUno juego = new JuegoUno();
        juego.agregarJugador("Ana");
        juego.agregarJugador("Robot 1");
        juego.agregarJugador("Robot 2");
        juego.agregarJugador("Robot 3");
        juego.iniciarJuego();
        Jugador humano = juego.getJugadores().get(0);
        AccionesFalsas acciones = new AccionesFalsas();

        assertThrows(IllegalArgumentException.class, () -> new TableroPanel(null, humano, acciones));
        TableroPanel tablero = new TableroPanel(juego, humano, acciones);
        tablero.setSize(1200, 800);
        assertTrue(tablero.esTurnoHumano());

        for (int i = 0; i < humano.cantidadCartas(); i++) {
            Point p = tablero.puntoVisible(i);
            assertSame(humano.obtenerMano().get(i), tablero.cartaEnPunto(p.x, p.y));
        }
        assertNull(tablero.cartaEnPunto(5, 5));
        assertTrue(tablero.areaCarta(0).x >= 0);

        Point p2 = tablero.puntoVisible(2);
        clic(tablero, p2.x, p2.y);
        assertSame(humano.obtenerMano().get(2), acciones.jugada);

        clic(tablero, 1140, 33);
        assertEquals(1, acciones.menus);
        clic(tablero, 1118, 692);
        assertEquals(1, acciones.unos);
        clic(tablero, 455, 380);
        assertEquals(1, acciones.robadas);

        for (int i = 0; i < 15; i++) {
            humano.agregarCarta(new Carta("ROJO", "1"));
        }
        tablero.actualizar();
        int ultima = humano.cantidadCartas() - 1;
        Rectangle area = tablero.areaCarta(ultima);
        assertTrue(area.x + area.width <= tablero.getWidth());
        for (int i = 0; i <= ultima; i++) {
            Point p = tablero.puntoVisible(i);
            assertSame(humano.obtenerMano().get(i), tablero.cartaEnPunto(p.x, p.y));
        }

        acciones.jugada = null;
        juego.robarCarta();
        tablero.actualizar();
        assertFalse(tablero.esTurnoHumano());
        Point p0 = tablero.puntoVisible(0);
        clic(tablero, p0.x, p0.y);
        assertNull(acciones.jugada);

        BufferedImage img = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
        tablero.mostrarToast("Hola", false);
        tablero.paint(img.getGraphics());
        tablero.mostrarToast("Error", true);
        tablero.paint(img.getGraphics());
        tablero.reiniciarVista();
        tablero.paint(img.getGraphics());

        Jugador robot = juego.getJugadores().get(1);
        Carta ultimaDelRobot = new Carta(juego.getColorActual(), "3");
        robot.limpiarMano();
        robot.agregarCarta(ultimaDelRobot);
        juego.jugarCarta(ultimaDelRobot);
        tablero.actualizar();
        tablero.paint(img.getGraphics());
        clic(tablero, p0.x, p0.y);
        assertNull(acciones.jugada);
        clic(tablero, 1140, 33);
        assertEquals(2, acciones.menus);
    }
}

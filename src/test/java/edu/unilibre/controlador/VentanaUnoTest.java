package edu.unilibre.controlador;

import edu.unilibre.modelo.Carta;
import edu.unilibre.modelo.JuegoUno;
import edu.unilibre.modelo.Jugador;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class VentanaUnoTest {

    private Object campo(VentanaUno ventana, String nombre) {
        try {
            Field campo = VentanaUno.class.getDeclaredField(nombre);
            campo.setAccessible(true);
            return campo.get(ventana);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private JuegoUno juegoDe(VentanaUno ventana) {
        try {
            Field campo = VentanaUno.class.getDeclaredField("juego");
            campo.setAccessible(true);
            return (JuegoUno) campo.get(ventana);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void iniciaJuegaUnTurnoTerminaUnaRondaYVuelveAlMenu() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla");
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            VentanaUno ventana = new VentanaUno();
            assertDoesNotThrow(() -> ventana.iniciar("Ana", 0));
            ventana.iniciar("Ana", 3);
            ventana.robar();
            ventana.robot();
            ventana.jugarTurnoRobot();
            ventana.decirUno();

            ventana.iniciar("Ana", 3);
            JuegoUno juego = juegoDe(ventana);
            Jugador ana = juego.getJugadores().get(0);
            for (Jugador robot : juego.getJugadores().subList(1, 4)) {
                robot.limpiarMano();
                robot.agregarCarta(new Carta("ROJO", "1"));
            }
            Carta ultima = new Carta(juego.getColorActual(), "3");
            ana.limpiarMano();
            ana.agregarCarta(ultima);
            ventana.jugar(ultima);
            assertTrue(juego.hayGanador());

            javax.swing.JPanel pantalla = (javax.swing.JPanel) campo(ventana, "pantallaJuego");
            javax.swing.JPanel resultado = (javax.swing.JPanel) campo(ventana, "panelResultado");
            assertTrue(resultado.isVisible());
            assertFalse(pantalla.isOptimizedDrawingEnabled(), "La tarjeta de resultado se superpone a la mesa");

            ventana.botonFin();
            assertEquals(2, juego.getNumeroRonda());
            assertFalse(juego.hayGanador());

            ventana.volverMenu();
            ventana.dispose();
        });
    }
}

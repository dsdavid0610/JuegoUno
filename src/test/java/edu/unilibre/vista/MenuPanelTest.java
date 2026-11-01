package edu.unilibre.vista;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuPanelTest {

    @Test
    void validaElNombreYArrancaConLosRobotsElegidos() {
        assertThrows(IllegalArgumentException.class, () -> new MenuPanel(null));

        List<String> llamadas = new ArrayList<>();
        MenuPanel menu = new MenuPanel((nombre, robots) -> llamadas.add(nombre + ":" + robots));
        assertEquals(3, menu.getCantidadRobots());

        menu.cambiarRobots(5);
        assertEquals(3, menu.getCantidadRobots());
        menu.cambiarRobots(-10);
        assertEquals(1, menu.getCantidadRobots());
        menu.cambiarRobots(2);
        assertEquals(3, menu.getCantidadRobots());

        menu.getCampoNombre().setText("   ");
        menu.arrancar();
        assertTrue(llamadas.isEmpty());
        assertTrue(menu.getLblError().getText().contains("nombre"));

        menu.getCampoNombre().setText("  Ana ");
        menu.arrancar();
        assertEquals(List.of("Ana:3"), llamadas);
        assertEquals(" ", menu.getLblError().getText());
    }
}

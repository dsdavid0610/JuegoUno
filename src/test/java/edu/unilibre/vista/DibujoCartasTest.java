package edu.unilibre.vista;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class DibujoCartasTest {

    private BufferedImage dibujar(String color, String valor) {
        BufferedImage img = new BufferedImage(100 + 20, 146 + 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        DibujoCartas.dibujarCartas(g, 10, 10, color, valor);
        g.dispose();
        return img;
    }

    private int fondo(BufferedImage img) {
        return img.getRGB(10 + 100 / 2, 10 + 9);
    }

    @Test
    void dibujaCartasConSusColoresYEtiquetas() {
        assertNotEquals(DibujoCartas.colorDe("ROJO"), DibujoCartas.colorDe("AZUL"));
        assertEquals("+2", DibujoCartas.etiqueta("MAS_DOS"));
        assertEquals("+4", DibujoCartas.etiqueta("MAS_CUATRO"));
        assertEquals("7", DibujoCartas.etiqueta("7"));

        BufferedImage azul = dibujar("AZUL", "3");
        Color pixel = new Color(fondo(azul), true);
        assertEquals(DibujoCartas.colorDe("AZUL"), new Color(pixel.getRed(), pixel.getGreen(), pixel.getBlue()));
        assertEquals(0, azul.getRGB(2, 2) >>> 24, "Fuera de la carta no se pinta");

        for (String[] carta : new String[][]{{null, null}, {"CAMBIO_COLOR", "MAS_CUATRO"},
                {"CAMBIO_COLOR", "CAMBIO_COLOR"}, {"AMARILLO", "SALTO"}, {"VERDE", "REVERSA"}, {"ROJO", "MAS_DOS"}}) {
            assertNotEquals(0, fondo(dibujar(carta[0], carta[1])) >>> 24);
        }
    }
}

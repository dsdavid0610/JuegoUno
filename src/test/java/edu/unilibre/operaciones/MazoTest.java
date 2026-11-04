package edu.unilibre.operaciones;

import edu.unilibre.modelo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MazoTest {

    // El constructor de Mazo es privado (como en el diagrama): solo JuegoUno lo crea.
    private JuegoUno.Mazo nuevoMazo() throws Exception {
        java.lang.reflect.Constructor<JuegoUno.Mazo> c = JuegoUno.Mazo.class.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    private List<Carta> sacarTodas(JuegoUno.Mazo mazo) {
        List<Carta> todas = new ArrayList<>();
        while (mazo.cantidadCartas() > 0) {
            todas.add(mazo.robar());
        }
        return todas;
    }

    @Test
    void elMazoTiene108CartasConLaComposicionCorrecta() throws Exception {
        assertEquals(108, nuevoMazo().cantidadCartas());
        List<Carta> todas = sacarTodas(nuevoMazo());

        Map<String, Long> porValor = todas.stream()
                .collect(Collectors.groupingBy(Carta::obtenerValor, Collectors.counting()));
        assertEquals(4L, porValor.get("0"));
        assertEquals(8L, porValor.get("5"));
        assertEquals(8L, porValor.get("SALTO"));
        assertEquals(8L, porValor.get("REVERSA"));
        assertEquals(8L, porValor.get("MAS_DOS"));
        assertEquals(4L, porValor.get("MAS_CUATRO"));
        assertEquals(4L, porValor.get("CAMBIO_COLOR"));

        Map<String, Long> porColor = todas.stream()
                .collect(Collectors.groupingBy(Carta::obtenerColor, Collectors.counting()));
        assertEquals(25L, porColor.get("ROJO"));
        assertEquals(25L, porColor.get("AMARILLO"));
        assertEquals(25L, porColor.get("VERDE"));
        assertEquals(25L, porColor.get("AZUL"));
        assertEquals(8L, porColor.get("CAMBIO_COLOR"));
    }

    @Test
    void robarBarajarReponerYReiniciar() throws Exception {
        JuegoUno.Mazo mazo = nuevoMazo();
        assertNotNull(mazo.robar());
        assertEquals(107, mazo.cantidadCartas());

        mazo.barajar();
        assertEquals(107, mazo.cantidadCartas());

        Carta extra = new Carta("ROJO", "1");
        mazo.reponer(List.of(extra, extra));
        assertEquals(109, mazo.cantidadCartas());
        assertThrows(IllegalArgumentException.class, () -> mazo.reponer(null));

        mazo.reiniciar();
        assertEquals(108, mazo.cantidadCartas());

        sacarTodas(mazo);
        assertThrows(IllegalStateException.class, mazo::robar);
    }
}

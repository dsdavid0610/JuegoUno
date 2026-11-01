package edu.unilibre.modelo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


public class RobotEstandar implements EstrategiaRobot {

    private static final List<String> colores = Arrays.stream(ColorCarta.values())
            .filter(color -> color != ColorCarta.CAMBIO_COLOR)
            .map(ColorCarta::name)
            .toList();

    private final Random azar = new Random();

    @Override
    public Optional<Carta> elegirCarta(JuegoUno juego, Jugador robot) {
        List<Carta> jugables = robot.obtenerMano().stream()
                .filter(carta -> esJugable(juego, robot, carta))
                .toList();
        if (jugables.isEmpty()) {
            return Optional.empty();
        }
        List<Carta> normales = jugables.stream().filter(carta -> !carta.esComodin()).toList();
        List<Carta> opciones = normales.isEmpty() ? jugables : normales;
        return Optional.of(opciones.get(azar.nextInt(opciones.size())));
    }

    private boolean esJugable(JuegoUno juego, Jugador robot, Carta carta) {
        if (juego.getPenalizacionAcumulada() > 0) {
            return carta.esMasDos() || carta.esMasCuatro();
        }
        if (!carta.sePuedeJugarSobre(juego.getCartaSuperior(), juego.getColorActual())) {
            return false;
        }
        return !carta.esMasCuatro() || !robot.tieneCartaDeColor(juego.getColorActual());
    }

    @Override
    public String elegirColor(Jugador robot) {
        Map<String, Long> conteo = colores.stream().collect(Collectors.toMap(
                color -> color,
                color -> robot.obtenerMano().stream()
                        .filter(carta -> carta.obtenerColor().equals(color))
                        .count()));
        long maximo = Collections.max(conteo.values());
        List<String> mejores = colores.stream().filter(color -> conteo.get(color) == maximo).toList();
        return mejores.get(azar.nextInt(mejores.size()));
    }
}

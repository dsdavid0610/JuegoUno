package edu.unilibre.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Mazo {

    private final List<Carta> cartas;

    public Mazo() {
        this.cartas = new ArrayList<>();
        reiniciar();
    }

    public void reiniciar() {
        cartas.clear();
        TipoCarta[] conAccion = {TipoCarta.SALTO, TipoCarta.REVERSA, TipoCarta.MAS_DOS};
        Arrays.stream(ColorCarta.values())
                .filter(color -> color != ColorCarta.CAMBIO_COLOR)
                .forEach(color -> {
                    cartas.add(new Carta(color.name(), "0"));
                    for (int numero = 1; numero <= 9; numero++) {
                        cartas.add(new Carta(color.name(), String.valueOf(numero)));
                        cartas.add(new Carta(color.name(), String.valueOf(numero)));
                    }
                    for (TipoCarta tipo : conAccion) {
                        cartas.add(new Carta(color.name(), tipo.name()));
                        cartas.add(new Carta(color.name(), tipo.name()));
                    }
                });
        for (int i = 0; i < 4; i++) {
            cartas.add(new Carta(ColorCarta.CAMBIO_COLOR.name(), TipoCarta.CAMBIO_COLOR.name()));
            cartas.add(new Carta(ColorCarta.CAMBIO_COLOR.name(), TipoCarta.MAS_CUATRO.name()));
        }
        barajar();
    }

    public void barajar() {
        Collections.shuffle(cartas);
    }

    public Carta robar() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("El mazo está vacío");
        }
        return cartas.remove(cartas.size() - 1);
    }

    public void reponer(List<Carta> nuevaCarta) {
        if (nuevaCarta == null) {
            throw new IllegalArgumentException("La lista de cartas es obligatoria");
        }
        cartas.addAll(nuevaCarta);
        barajar();
    }

    public int cantidadCartas() {
        return cartas.size();
    }
}

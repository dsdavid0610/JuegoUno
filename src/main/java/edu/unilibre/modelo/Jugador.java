package edu.unilibre.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Jugador {

    private final String nombre;
    private final List<Carta> mano;
    private int puntos;

    public Jugador(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador es obligatorio");
        }
        this.nombre = nombre.trim();
        this.mano = new ArrayList<>();
        this.puntos = 0;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public List<Carta> obtenerMano() {
        return Collections.unmodifiableList(mano);
    }

    public int obtenerPuntos() {
        return puntos;
    }

    public void agregarCarta(Carta carta) {
        if (carta == null) {
            throw new IllegalArgumentException("La carta es obligatoria");
        }
        mano.add(carta);
    }

    public void recibirCarta(Carta carta) {
        agregarCarta(carta);
    }

    public void eliminarCarta(Carta carta) {
        if (!mano.remove(carta)) {
            throw new IllegalArgumentException("La carta no está en la mano de " + nombre);
        }
    }

    public boolean tieneCarta(Carta carta) {
        return mano.contains(carta);
    }

    public int cantidadCartas() {
        return mano.size();
    }

    public boolean gano() {
        return mano.isEmpty();
    }

    public void limpiarMano() {
        mano.clear();
    }

    public void sumarPuntos(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("No se pueden sumar puntos negativos");
        }
        puntos += cantidad;
    }

    public void reiniciarPuntos() {
        puntos = 0;
    }

    public boolean puedeJugar(Carta cartaSuperior) {
        return mano.stream().anyMatch(carta -> carta.sePuedeJugarSobre(cartaSuperior));
    }

    public boolean puedeJugar(Carta cartaSuperior, String colorActual) {
        return mano.stream().anyMatch(carta -> carta.sePuedeJugarSobre(cartaSuperior, colorActual));
    }

    public boolean tieneCartaDeColor(String color) {
        return mano.stream().anyMatch(carta -> carta.obtenerColor().equals(color));
    }

    public int calcularPuntosMano() {
        return mano.stream().mapToInt(Carta::puntos).sum();
    }

    @Override
    public String toString() {
        return nombre + " (" + mano.size() + " cartas, " + puntos + " puntos)";
    }
}

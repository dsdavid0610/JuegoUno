package edu.unilibre.modelo;

import java.util.Arrays;

public class Carta {

    private final String color;
    private final String valor;


    public Carta(String color, String valor) {
        if (color == null || valor == null) {
            throw new IllegalArgumentException("El color y el valor de la carta son obligatorios");
        }
        boolean colorValido = Arrays.stream(ColorCarta.values()).anyMatch(c -> c.name().equals(color));
        if (!colorValido) {
            throw new IllegalArgumentException("Color de carta no válido: " + color);
        }
        boolean esComodinColor = ColorCarta.CAMBIO_COLOR.name().equals(color);
        boolean esComodinValor = TipoCarta.CAMBIO_COLOR.name().equals(valor)
                || TipoCarta.MAS_CUATRO.name().equals(valor);
        if (esComodinColor != esComodinValor) {
            throw new IllegalArgumentException("Combinación de color y valor no válida: " + color + " " + valor);
        }
        if (!esNumero(valor) && !esTipoEspecial(valor)) {
            throw new IllegalArgumentException("Valor de carta no válido: " + valor);
        }
        this.color = color;
        this.valor = valor;
    }

    private static boolean esNumero(String valor) {
        return valor.length() == 1 && Character.isDigit(valor.charAt(0));
    }

    private static boolean esTipoEspecial(String valor) {
        return Arrays.stream(TipoCarta.values())
                .filter(t -> t != TipoCarta.NUMERO)
                .anyMatch(t -> t.name().equals(valor));
    }

    public String obtenerColor() {
        return color;
    }

    public String obtenerValor() {
        return valor;
    }

    public boolean esSalta() {
        return TipoCarta.SALTO.name().equals(valor);
    }

    public boolean esReversa() {
        return TipoCarta.REVERSA.name().equals(valor);
    }

    public boolean esMasDos() {
        return TipoCarta.MAS_DOS.name().equals(valor);
    }

    public boolean esMasCuatro() {
        return TipoCarta.MAS_CUATRO.name().equals(valor);
    }

    public boolean esCambioColor() {
        return TipoCarta.CAMBIO_COLOR.name().equals(valor);
    }

    public boolean esComodin() {
        return esMasCuatro() || esCambioColor();
    }

    public boolean esEspecial() {
        return esSalta() || esReversa() || esMasDos() || esComodin();
    }


    public int puntos() {
        if (esCambioColor()) {
            return 50;
        }
        if (esEspecial()) {
            return 20;
        }
        return 0;
    }


    public boolean sePuedeJugarSobre(Carta cartaSuperior) {
        if (cartaSuperior == null) {
            throw new IllegalArgumentException("No hay carta sobre la mesa");
        }
        return esComodin() || color.equals(cartaSuperior.color) || valor.equals(cartaSuperior.valor);
    }

    public boolean sePuedeJugarSobre(Carta cartaSuperior, String colorActual) {
        if (cartaSuperior == null) {
            throw new IllegalArgumentException("No hay carta sobre la mesa");
        }
        return esComodin() || color.equals(colorActual) || valor.equals(cartaSuperior.valor);
    }

    @Override
    public String toString() {
        if (esMasCuatro()) {
            return "COMODIN +4";
        }
        if (esCambioColor()) {
            return "COMODIN COLOR";
        }
        String etiqueta;
        if (esMasDos()) {
            etiqueta = "+2";
        } else if (esSalta()) {
            etiqueta = "SALTA";
        } else if (esReversa()) {
            etiqueta = "REVERSA";
        } else {
            etiqueta = valor;
        }
        return color + " " + etiqueta;
    }
}

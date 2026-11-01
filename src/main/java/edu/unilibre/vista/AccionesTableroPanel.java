package edu.unilibre.vista;

import edu.unilibre.modelo.Carta;


public interface AccionesTableroPanel {

    void jugar(Carta carta);

    void robar();

    void decirUno();

    void botonFin();

    void volverMenu();
}

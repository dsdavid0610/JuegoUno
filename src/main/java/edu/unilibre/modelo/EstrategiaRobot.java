package edu.unilibre.modelo;

import java.util.Optional;


public interface EstrategiaRobot {
    Optional<Carta> elegirCarta(JuegoUno juego, Jugador robot);
    String elegirColor(Jugador robot);
}

package edu.unilibre.dto;

import java.time.LocalDate;

import lombok.*;

public interface PartidaDTO {
    int getId();

    LocalDate getFecha();

    String getNombreHumano();

    String getNombreGanador();

    boolean isGanoHumano();

    int getPuntosGanador();

    int getRondas();

    static PartidaDTO crear(int id, LocalDate fecha, String nombreHumano, String nombreGanador,
                            boolean ganoHumano, int puntosGanador, int rondas) {
        return new Implementacion(id, fecha, nombreHumano, nombreGanador,
                ganoHumano, puntosGanador, rondas);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Implementacion implements PartidaDTO {
        private final int id;
        private final LocalDate fecha;
        private final String nombreHumano;
        private final String nombreGanador;
        private final boolean ganoHumano;
        private final int puntosGanador;
        private final int rondas;

        @Override
        public String toString() {
            return "Partida #" + id + " (" + fecha + "): " + nombreHumano + " - ganó " + nombreGanador
                    + " con " + puntosGanador + " puntos en " + rondas + " rondas";
        }
    }
}
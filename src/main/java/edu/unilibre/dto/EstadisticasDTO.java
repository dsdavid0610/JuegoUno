package edu.unilibre.dto;

import lombok.*;

public interface EstadisticasDTO {
    int getPartidas();
    int getVictorias();
    int getDerrotas();
    double getRondasPromedio();

    default boolean eliminar(int id) {
        return false;
    }

    static EstadisticasDTO crear(int partidas, int victorias, int derrotas, double rondasPromedio) {
        return new Implementacion(partidas, victorias, derrotas, rondasPromedio);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Implementacion implements EstadisticasDTO {
        private final int partidas;
        private final int victorias;
        private final int derrotas;
        private final double rondasPromedio;

        @Override
        public String toString() {
            return String.format("Partidas: %d | Victorias: %d | Derrotas: %d | Rondas promedio: %.1f",
                    partidas, victorias, derrotas, rondasPromedio);
        }
    }
}

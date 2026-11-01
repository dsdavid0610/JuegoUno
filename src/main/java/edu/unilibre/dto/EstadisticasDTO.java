package edu.unilibre.dto;


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

    final class Implementacion implements EstadisticasDTO {
        private final int partidas;
        private final int victorias;
        private final int derrotas;
        private final double rondasPromedio;

        private Implementacion(int partidas, int victorias, int derrotas, double rondasPromedio) {
            this.partidas = partidas;
            this.victorias = victorias;
            this.derrotas = derrotas;
            this.rondasPromedio = rondasPromedio;
        }

        public int getPartidas() { return partidas; }
        public int getVictorias() { return victorias; }
        public int getDerrotas() { return derrotas; }
        public double getRondasPromedio() { return rondasPromedio; }

        @Override
        public String toString() {
            return String.format("Partidas: %d | Victorias: %d | Derrotas: %d | Rondas promedio: %.1f",
                    partidas, victorias, derrotas, rondasPromedio);
        }
    }
}

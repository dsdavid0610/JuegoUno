package edu.unilibre.dto;

import java.time.LocalDate;

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

    final class Implementacion implements PartidaDTO {
        private final int id;
        private final LocalDate fecha;
        private final String nombreHumano;
        private final String nombreGanador;
        private final boolean ganoHumano;
        private final int puntosGanador;
        private final int rondas;

        private Implementacion(int id, LocalDate fecha, String nombreHumano, String nombreGanador,
                               boolean ganoHumano, int puntosGanador, int rondas) {
            this.id = id;
            this.fecha = fecha;
            this.nombreHumano = nombreHumano;
            this.nombreGanador = nombreGanador;
            this.ganoHumano = ganoHumano;
            this.puntosGanador = puntosGanador;
            this.rondas = rondas;
        }

        public int getId() { return id; }
        public LocalDate getFecha() { return fecha; }
        public String getNombreHumano() { return nombreHumano; }
        public String getNombreGanador() { return nombreGanador; }
        public boolean isGanoHumano() { return ganoHumano; }
        public int getPuntosGanador() { return puntosGanador; }
        public int getRondas() { return rondas; }

        @Override
        public String toString() {
            return "Partida #" + id + " (" + fecha + "): " + nombreHumano + " - ganó " + nombreGanador
                    + " con " + puntosGanador + " puntos en " + rondas + " rondas";
        }
    }
}

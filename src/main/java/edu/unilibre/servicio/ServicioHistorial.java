package edu.unilibre.servicio;

import edu.unilibre.dao.PartidaDAO;
import edu.unilibre.dto.EstadisticasDTO;
import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.modelo.JuegoUno;
import edu.unilibre.modelo.Jugador;

import javax.swing.JLabel;
import java.time.LocalDate;
import java.util.List;


public class ServicioHistorial {

    private final PartidaDAO dao;
    private final JLabel lblError;
    private int cantidadRobots;

    public ServicioHistorial(PartidaDAO dao) {
        if (dao == null) {
            throw new IllegalArgumentException("El DAO es obligatorio");
        }
        this.dao = dao;
        this.lblError = new JLabel("");
    }

    public PartidaDTO registrarPartida(JuegoUno juego) {
        Jugador ganador = juego.getGanadorPartida();
        if (ganador == null) {
            lblError.setText("La partida todavía no tiene ganador");
            throw new IllegalStateException("La partida todavía no tiene ganador");
        }
        Jugador humano = juego.getJugadores().get(0);
        PartidaDTO partida = PartidaDTO.crear(
                dao.siguienteId(),
                LocalDate.now(),
                humano.obtenerNombre(),
                ganador.obtenerNombre(),
                ganador == humano,
                ganador.obtenerPuntos(),
                juego.getNumeroRonda());
        dao.guardar(partida);
        cantidadRobots = juego.cantidadJugadores() - 1;
        lblError.setText("");
        return partida;
    }

    public List<PartidaDTO> listar() {
        return dao.listarTodos();
    }

    public EstadisticasDTO estadisticas() {
        List<PartidaDTO> partidas = dao.listarTodos();
        int total = partidas.size();
        int victorias = (int) partidas.stream().filter(PartidaDTO::isGanoHumano).count();
        double promedio = partidas.stream().mapToInt(PartidaDTO::getRondas).average().orElse(0);
        return EstadisticasDTO.crear(total, victorias, total - victorias, promedio);
    }

    public boolean borrar(int id) {
        return dao.eliminar(id);
    }

    public void limpiar() {
        dao.listarTodos().forEach(partida -> dao.eliminar(partida.getId()));
    }
}

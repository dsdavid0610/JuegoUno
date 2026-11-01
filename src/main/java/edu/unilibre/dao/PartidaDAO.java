package edu.unilibre.dao;

import edu.unilibre.dto.PartidaDTO;

import java.util.List;
import java.util.Optional;


public interface PartidaDAO {

    /** Siguiente identificador libre. */
    int siguienteId();

    List<PartidaDTO> buscarPorJugador(String nombre);

    void guardar(PartidaDTO partida);

    Optional<PartidaDTO> buscarPorId(int id);

    List<PartidaDTO> listarTodos();

    void actualizar(PartidaDTO partida);

    boolean eliminar(int id);
}
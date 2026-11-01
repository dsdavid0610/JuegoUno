package edu.unilibre.dao;

import edu.unilibre.modelo.Jugador;

import java.util.List;
import java.util.Optional;


public interface DAO {

    void guardar(Jugador jugador);

    Optional<Jugador> buscarPorId(int id);

    List<Jugador> listarTodos();

    void actualizar(Jugador jugador);

    boolean eliminar(int id);
}
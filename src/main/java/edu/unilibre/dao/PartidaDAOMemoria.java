package edu.unilibre.dao;

import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.servicio.ServicioHistorial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PartidaDAOMemoria implements PartidaDAO {

    private final List<PartidaDTO> datos = new ArrayList<>();


    public void operaciones(ServicioHistorial servicio) {
        servicio.listar().stream()
                .filter(partida -> buscarPorId(partida.getId()).isEmpty())
                .forEach(this::guardar);
    }

    @Override
    public void guardar(PartidaDTO partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida es obligatoria");
        }
        if (buscarPorId(partida.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una partida con id " + partida.getId());
        }
        datos.add(partida);
    }

    @Override
    public Optional<PartidaDTO> buscarPorId(int id) {
        return datos.stream().filter(partida -> partida.getId() == id).findFirst();
    }

    @Override
    public List<PartidaDTO> listarTodos() {
        return new ArrayList<>(datos);
    }

    @Override
    public void actualizar(PartidaDTO partida) {
        if (partida == null || buscarPorId(partida.getId()).isEmpty()) {
            throw new IllegalArgumentException("No existe la partida que se quiere actualizar");
        }
        datos.replaceAll(actual -> actual.getId() == partida.getId() ? partida : actual);
    }

    @Override
    public boolean eliminar(int id) {
        return datos.removeIf(partida -> partida.getId() == id);
    }

    @Override
    public int siguienteId() {
        return datos.stream().mapToInt(PartidaDTO::getId).max().orElse(0) + 1;
    }

    @Override
    public List<PartidaDTO> buscarPorJugador(String nombre) {
        return datos.stream()
                .filter(partida -> partida.getNombreHumano().equalsIgnoreCase(nombre))
                .toList();
    }
}

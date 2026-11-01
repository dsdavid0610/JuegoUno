package edu.unilibre.dao;

import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.servicio.ServicioHistorial;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PartidaDAOArchivo implements PartidaDAO {

    private static final String separador = ";";

    private final Path archivo;
    private final PartidaDAOMemoria memoria;
    private int cantidadRobots;

    public PartidaDAOArchivo(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria");
        }
        this.archivo = archivo;
        this.memoria = new PartidaDAOMemoria();
        cargar();
    }

    public void operaciones(ServicioHistorial servicio) {
        servicio.listar().stream()
                .filter(partida -> buscarPorId(partida.getId()).isEmpty())
                .forEach(this::guardar);
    }

    public void cargar() {
        memoria.listarTodos().forEach(partida -> memoria.eliminar(partida.getId()));
        if (!Files.exists(archivo)) {
            return;
        }
        try {
            Files.readAllLines(archivo, StandardCharsets.UTF_8).stream()
                    .filter(linea -> !linea.isBlank())
                    .map(this::deLinea)
                    .flatMap(Optional::stream)
                    .forEach(memoria::guardar);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + archivo, e);
        }
    }

    public void escribir() {
        List<String> lineas = memoria.listarTodos().stream().map(this::aLinea).toList();
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir " + archivo, e);
        }
    }

    private String aLinea(PartidaDTO p) {
        return String.join(separador,
                String.valueOf(p.getId()),
                p.getFecha().toString(),
                limpiar(p.getNombreHumano()),
                limpiar(p.getNombreGanador()),
                String.valueOf(p.isGanoHumano()),
                String.valueOf(p.getPuntosGanador()),
                String.valueOf(p.getRondas()));
    }

    private Optional<PartidaDTO> deLinea(String linea) {
        String[] campos = linea.split(separador, -1);
        if (campos.length != 7) {
            return Optional.empty();
        }
        try {
            return Optional.of(PartidaDTO.crear(
                    Integer.parseInt(campos[0]),
                    LocalDate.parse(campos[1]),
                    campos[2],
                    campos[3],
                    Boolean.parseBoolean(campos[4]),
                    Integer.parseInt(campos[5]),
                    Integer.parseInt(campos[6])));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private String limpiar(String texto) {
        return texto.replace(separador, ",").replace("\n", " ").replace("\r", " ");
    }

    @Override
    public void guardar(PartidaDTO partida) {
        memoria.guardar(partida);
        escribir();
    }

    @Override
    public Optional<PartidaDTO> buscarPorId(int id) {
        return memoria.buscarPorId(id);
    }

    @Override
    public List<PartidaDTO> listarTodos() {
        return memoria.listarTodos();
    }

    @Override
    public void actualizar(PartidaDTO partida) {
        memoria.actualizar(partida);
        escribir();
    }

    @Override
    public boolean eliminar(int id) {
        boolean eliminada = memoria.eliminar(id);
        if (eliminada) {
            escribir();
        }
        return eliminada;
    }

    @Override
    public int siguienteId() {
        return memoria.siguienteId();
    }

    @Override
    public List<PartidaDTO> buscarPorJugador(String nombre) {
        return memoria.buscarPorJugador(nombre);
    }
}

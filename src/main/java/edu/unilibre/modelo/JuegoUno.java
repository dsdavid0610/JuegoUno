package edu.unilibre.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class JuegoUno {

    private final Mazo mazo;
    private final List<Jugador> jugadores;
    private final List<Carta> pila;
    private int turno;
    private boolean sentidoHorario;
    private boolean rondaIniciada;
    private boolean rondaTerminada;
    private boolean unoPendiente;
    private Jugador jugadorQueDebeDecirUno;
    private boolean penalizadoPorUno;
    private Jugador jugadorPenalizadoUno;
    private int puntosGanador;
    private int numeroRonda;
    private Jugador ganadorPartida;
    private String colorActual;
    private boolean colorPendiente;
    private int penalizacionAcumulada;

    public JuegoUno() {
        this.mazo = new Mazo();
        this.jugadores = new ArrayList<>();
        this.pila = new ArrayList<>();
        reiniciarEstado();
    }

    // ------------------------------------------------------------------ jugadores

    public void agregarJugador(String nombre) {
        if (rondaIniciada) {
            throw new IllegalStateException("No se pueden agregar jugadores con la partida en curso");
        }
        if (jugadores.size() >= 10) {
            throw new IllegalStateException("Máximo 10 jugadores");
        }
        jugadores.add(new Jugador(nombre));
    }

    public void limpiarJugadores() {
        jugadores.clear();
        pila.clear();
        reiniciarEstado();
    }

    public List<Jugador> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    public int cantidadJugadores() {
        return jugadores.size();
    }

    // ------------------------------------------------------------------ inicio de partida y rondas

    public void iniciarJuego() {
        if (jugadores.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores");
        }
        jugadores.forEach(Jugador::reiniciarPuntos);
        numeroRonda = 0;
        ganadorPartida = null;
        iniciarRonda();
    }

    public void nuevaRonda() {
        if (!rondaIniciada) {
            throw new IllegalStateException("Primero debes iniciar el juego");
        }
        if (ganadorPartida != null) {
            throw new IllegalStateException("La partida ya terminó");
        }
        if (!rondaTerminada) {
            throw new IllegalStateException("La ronda actual todavía no termina");
        }
        iniciarRonda();
    }

    private void iniciarRonda() {
        mazo.reiniciar();
        pila.clear();
        jugadores.forEach(Jugador::limpiarMano);
        for (int i = 0; i < 7; i++) {
            for (Jugador jugador : jugadores) {
                jugador.agregarCarta(mazo.robar());
            }
        }
        List<Carta> descartadas = new ArrayList<>();
        Carta inicial = mazo.robar();
        while (inicial.esEspecial()) {
            descartadas.add(inicial);
            inicial = mazo.robar();
        }
        mazo.reponer(descartadas);
        pila.add(inicial);

        colorActual = inicial.obtenerColor();
        turno = 0;
        sentidoHorario = true;
        numeroRonda++;
        rondaIniciada = true;
        rondaTerminada = false;
        puntosGanador = 0;
        unoPendiente = false;
        jugadorQueDebeDecirUno = null;
        penalizadoPorUno = false;
        jugadorPenalizadoUno = null;
        colorPendiente = false;
        penalizacionAcumulada = 0;
    }

    private void reiniciarEstado() {
        turno = 0;
        sentidoHorario = true;
        rondaIniciada = false;
        rondaTerminada = false;
        unoPendiente = false;
        jugadorQueDebeDecirUno = null;
        penalizadoPorUno = false;
        jugadorPenalizadoUno = null;
        puntosGanador = 0;
        numeroRonda = 0;
        ganadorPartida = null;
        colorActual = null;
        colorPendiente = false;
        penalizacionAcumulada = 0;
    }

    // ------------------------------------------------------------------ jugadas

    public void jugarCarta(Carta carta) {
        validarRondaActiva();
        if (carta == null) {
            throw new IllegalArgumentException("Debes elegir una carta");
        }
        if (colorPendiente) {
            throw new IllegalStateException("Debes elegir un color antes de continuar");
        }
        Jugador actual = getJugadorActual();
        if (!actual.tieneCarta(carta)) {
            throw new IllegalArgumentException("Esa carta no está en la mano de " + actual.obtenerNombre());
        }
        validarJugada(actual, carta);

        resolverUnoPendiente(actual);
        actual.eliminarCarta(carta);
        pila.add(carta);

        if (actual.gano()) {
            terminarRonda(actual);
            return;
        }
        if (actual.cantidadCartas() == 1) {
            unoPendiente = true;
            jugadorQueDebeDecirUno = actual;
        }
        aplicarEfecto(carta);
    }

    private void validarJugada(Jugador actual, Carta carta) {
        if (penalizacionAcumulada > 0) {
            if (!carta.esMasDos() && !carta.esMasCuatro()) {
                throw new IllegalArgumentException("Debes responder con un +2 o un +4, o robar "
                        + penalizacionAcumulada + " cartas");
            }
            return;
        }
        if (!carta.sePuedeJugarSobre(getCartaSuperior(), colorActual)) {
            throw new IllegalArgumentException("Carta no válida: no coincide con " + getCartaSuperior()
                    + " (color activo " + colorActual + ")");
        }
        if (carta.esMasCuatro() && actual.tieneCartaDeColor(colorActual)) {
            throw new IllegalArgumentException("El +4 solo se puede jugar si no tienes cartas del color activo");
        }
    }

    private void aplicarEfecto(Carta carta) {
        if (carta.esCambioColor()) {
            colorPendiente = true;
            return;
        }
        if (carta.esMasCuatro()) {
            penalizacionAcumulada += 4;
            colorPendiente = true;
            return;
        }
        colorActual = carta.obtenerColor();
        if (carta.esMasDos()) {
            penalizacionAcumulada += 2;
            avanzarTurno(1);
        } else if (carta.esSalta()) {
            avanzarTurno(2);
        } else if (carta.esReversa()) {
            sentidoHorario = !sentidoHorario;
            avanzarTurno(jugadores.size() == 2 ? 2 : 1);
        } else {
            avanzarTurno(1);
        }
    }

    public void cambiarColor(String color) {
        validarRondaActiva();
        if (!colorPendiente) {
            throw new IllegalStateException("No hay un cambio de color pendiente");
        }
        boolean valido = Arrays.stream(ColorCarta.values())
                .filter(c -> c != ColorCarta.CAMBIO_COLOR)
                .anyMatch(c -> c.name().equals(color));
        if (!valido) {
            throw new IllegalArgumentException("Color no válido: " + color);
        }
        colorActual = color;
        colorPendiente = false;
        avanzarTurno(1);
    }

    public Carta robarCarta() {
        validarRondaActiva();
        if (colorPendiente) {
            throw new IllegalStateException("Debes elegir un color antes de continuar");
        }
        Jugador actual = getJugadorActual();
        resolverUnoPendiente(actual);

        int cantidad = penalizacionAcumulada > 0 ? penalizacionAcumulada : 1;
        Carta ultima = null;
        for (int i = 0; i < cantidad; i++) {
            Carta robada = robarDelMazo();
            if (robada != null) {
                actual.recibirCarta(robada);
                ultima = robada;
            }
        }
        penalizacionAcumulada = 0;
        avanzarTurno(1);
        return ultima;
    }


    public void decirUno() {
        validarRondaActiva();
        if (!unoPendiente) {
            throw new IllegalStateException("No tienes que decir UNO en este momento");
        }
        unoPendiente = false;
        jugadorQueDebeDecirUno = null;
    }


    private void resolverUnoPendiente(Jugador quienActua) {
        penalizadoPorUno = false;
        jugadorPenalizadoUno = null;
        if (!unoPendiente) {
            return;
        }
        if (jugadorQueDebeDecirUno != quienActua) {
            Jugador olvidadizo = jugadorQueDebeDecirUno;
            robarCartas(olvidadizo, 2);
            penalizadoPorUno = true;
            jugadorPenalizadoUno = olvidadizo;
        }
        unoPendiente = false;
        jugadorQueDebeDecirUno = null;
    }

    private void terminarRonda(Jugador ganador) {
        rondaTerminada = true;
        unoPendiente = false;
        jugadorQueDebeDecirUno = null;
        colorPendiente = false;
        penalizacionAcumulada = 0;
        puntosGanador = jugadores.stream()
                .filter(jugador -> jugador != ganador)
                .mapToInt(Jugador::calcularPuntosMano)
                .sum();
        ganador.sumarPuntos(puntosGanador);
        if (ganador.obtenerPuntos() >= 250) {
            ganadorPartida = ganador;
        }
    }

    // ------------------------------------------------------------------ mazo y turnos

    private Carta robarDelMazo() {
        if (mazo.cantidadCartas() == 0) {
            reponerMazoDesdePila();
        }
        return mazo.cantidadCartas() == 0 ? null : mazo.robar();
    }

    private void robarCartas(Jugador jugador, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Carta robada = robarDelMazo();
            if (robada != null) {
                jugador.recibirCarta(robada);
            }
        }
    }

    private void reponerMazoDesdePila() {
        if (pila.size() <= 1) {
            return;
        }
        Carta tope = pila.remove(pila.size() - 1);
        List<Carta> resto = new ArrayList<>(pila);
        pila.clear();
        pila.add(tope);
        mazo.reponer(resto);
    }

    private void avanzarTurno(int pasos) {
        int n = jugadores.size();
        int delta = sentidoHorario ? pasos : -pasos;
        turno = ((turno + delta) % n + n) % n;
    }

    private void validarRondaActiva() {
        if (!rondaIniciada) {
            throw new IllegalStateException("El juego todavía no ha iniciado");
        }
        if (rondaTerminada) {
            throw new IllegalStateException("La ronda ya terminó");
        }
    }

    // ------------------------------------------------------------------ consultas

    public Jugador getJugadorActual() {
        if (jugadores.isEmpty()) {
            throw new IllegalStateException("No hay jugadores");
        }
        return jugadores.get(turno);
    }

    public Carta getCartaSuperior() {
        return pila.isEmpty() ? null : pila.get(pila.size() - 1);
    }

    public String getColorActual() {
        return colorActual;
    }

    public boolean hayColorPendiente() {
        return colorPendiente;
    }

    public boolean hayUnoPendiente() {
        return unoPendiente;
    }

    public Jugador getJugadorQueDebeDecirUno() {
        return jugadorQueDebeDecirUno;
    }

    public boolean huboPenalizacionUno() {
        return penalizadoPorUno;
    }

    public Jugador getJugadorPenalizadoUno() {
        return jugadorPenalizadoUno;
    }

    public boolean hayGanador() {
        return rondaTerminada;
    }

    public Jugador getGanador() {
        if (!rondaTerminada) {
            return null;
        }
        return jugadores.stream().filter(Jugador::gano).findFirst().orElse(null);
    }

    public Jugador getGanadorPartida() {
        return ganadorPartida;
    }

    public int getPuntosGanadorRonda() {
        return puntosGanador;
    }

    public int getNumeroRonda() {
        return numeroRonda;
    }

    public int cartasEnMazo() {
        return mazo.cantidadCartas();
    }

    public int cartasEnPila() {
        return pila.size();
    }

    public int getPenalizacionAcumulada() {
        return penalizacionAcumulada;
    }

    public boolean puedeContrarrestarPenalizacion() {
        if (!rondaIniciada || rondaTerminada || penalizacionAcumulada == 0) {
            return false;
        }
        return getJugadorActual().obtenerMano().stream()
                .anyMatch(carta -> carta.esMasDos() || carta.esMasCuatro());
    }
}

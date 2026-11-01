package edu.unilibre.controlador;

import edu.unilibre.dao.PartidaDAOArchivo;
import edu.unilibre.dto.PartidaDTO;
import edu.unilibre.modelo.Carta;
import edu.unilibre.modelo.EstrategiaRobot;
import edu.unilibre.modelo.JuegoUno;
import edu.unilibre.modelo.Jugador;
import edu.unilibre.modelo.RobotEstandar;
import edu.unilibre.servicio.ServicioHistorial;
import edu.unilibre.vista.AccionesTableroPanel;
import edu.unilibre.vista.InicioMenuPanel;
import edu.unilibre.vista.MenuPanel;
import edu.unilibre.vista.TableroPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class VentanaUno extends JFrame implements InicioMenuPanel, AccionesTableroPanel {

    private final JuegoUno juego;
    private JTextField[] campos;
    private JLabel[] etiquetas;
    private JPanel panelMano;
    private JTextArea txtArea;
    private JLabel lblMesa;
    private JLabel lblTurno;
    private JLabel lblMazo;
    private int cantidadJugadoresVisibles;
    private final ServicioHistorial historial;
    private final EstrategiaRobot estrategia;
    private Timer temporizadorRobot;
    private final MenuPanel menu;
    private TableroPanel tablero;

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);
    private JPanel pantallaJuego;
    private JPanel panelResultado;
    private JButton btnFin;
    private JButton btnSecundario;
    private Jugador humano;
    private String nombreHumano;
    private int robotsActuales;

    public VentanaUno() {
        super("Juego - UNO");
        this.juego = new JuegoUno();
        this.estrategia = new RobotEstandar();
        this.historial = new ServicioHistorial(new PartidaDAOArchivo(Paths.get("historial_uno.csv")));
        this.txtArea = new JTextArea(16, 56);
        this.campos = new JTextField[4];
        this.etiquetas = new JLabel[4];
        this.menu = new MenuPanel(this, this::mostrarHistorial);

        contenedor.add(menu, "menu");
        contenedor.setPreferredSize(new Dimension(1050, 700));
        setContentPane(contenedor);
        cartas.show(contenedor, "menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 640));
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaUno().setVisible(true));
    }

    // ------------------------------------------------------------------ InicioMenuPanel

    @Override
    public void iniciar(String nombreHumano, int cantidadRobots) {
        try {
            if (cantidadRobots < 1 || cantidadRobots > 4 - 1) {
                throw new IllegalArgumentException("Debe haber entre 1 y 3 robots");
            }
            detenerTemporizador();
            juego.limpiarJugadores();
            juego.agregarJugador(nombreHumano);
            for (int i = 1; i <= cantidadRobots; i++) {
                juego.agregarJugador("Robot " + i);
            }
            juego.iniciarJuego();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            menu.mostrarError(ex.getMessage());
            return;
        }
        this.nombreHumano = nombreHumano;
        this.robotsActuales = cantidadRobots;
        humano = juego.getJugadores().get(0);
        cantidadJugadoresVisibles = juego.cantidadJugadores();
        construirPantallaJuego();
        cartas.show(contenedor, "juego");
        refrescar();
        robot();
    }

    // ------------------------------------------------------------------ AccionesTableroPanel

    @Override
    public void jugar(Carta carta) {
        if (juego.hayGanador() || !tablero.esTurnoHumano()) {
            tablero.mostrarToast("Todavía no es tu turno", true);
            return;
        }
        StringBuilder mensaje = new StringBuilder();
        boolean error;
        try {
            juego.jugarCarta(carta);
            mensaje.append("Jugaste ").append(describir(carta));
            error = anotarPenalizacionUno(mensaje);
            if (juego.hayColorPendiente()) {
                String color = pedirColor();
                juego.cambiarColor(color);
                mensaje.append(" · color ").append(nombreColor(color));
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            tablero.mostrarToast(ex.getMessage(), true);
            return;
        }
        tablero.mostrarToast(mensaje.toString(), error);
        despuesDeJugada();
    }


    @Override
    public void robar() {
        if (juego.hayGanador() || !tablero.esTurnoHumano()) {
            tablero.mostrarToast("Todavía no es tu turno", true);
            return;
        }
        int cantidad = Math.max(1, juego.getPenalizacionAcumulada());
        StringBuilder mensaje = new StringBuilder();
        boolean error;
        try {
            Carta robada = juego.robarCarta();
            if (cantidad == 1 && robada != null) {
                mensaje.append("Robaste: ").append(describir(robada));
            } else {
                mensaje.append("Robaste ").append(cantidad).append(" cartas");
            }
            error = anotarPenalizacionUno(mensaje);
        } catch (IllegalStateException ex) {
            tablero.mostrarToast(ex.getMessage(), true);
            return;
        }
        tablero.mostrarToast(mensaje.toString(), error);
        despuesDeJugada();
    }

    @Override
    public void decirUno() {
        if (tablero == null) {
            return;
        }
        try {
            juego.decirUno();
            tablero.mostrarToast("¡UNO!", false);
            tablero.actualizar();
        } catch (IllegalStateException ex) {
            tablero.mostrarToast(ex.getMessage(), true);
        }
    }

    /** Botón de fin de ronda: empieza la siguiente ronda o, si acabó la partida, vuelve al menú. */
    @Override
    public void botonFin() {
        if (!juego.hayGanador()) {
            return;
        }
        if (juego.getGanadorPartida() != null) {
            volverMenu();
            return;
        }
        juego.nuevaRonda();
        panelResultado.setVisible(false);
        tablero.reiniciarVista();
        refrescar();
        robot();
    }

    @Override
    public void volverMenu() {
        detenerTemporizador();
        cartas.show(contenedor, "menu");
    }

    // ------------------------------------------------------------------ turno de los robots


    public void jugarTurnoRobot() {
        if (juego.hayGanador()) {
            return;
        }
        Jugador robot = juego.getJugadorActual();
        if (robot == humano) {
            return;
        }
        StringBuilder mensaje = new StringBuilder();
        boolean error = false;
        try {
            Optional<Carta> eleccion = estrategia.elegirCarta(juego, robot);
            if (eleccion.isPresent()) {
                Carta carta = eleccion.get();
                juego.jugarCarta(carta);
                mensaje.append(robot.obtenerNombre()).append(" jugó ").append(describir(carta));
                error = anotarPenalizacionUno(mensaje);
                if (juego.hayColorPendiente()) {
                    String color = estrategia.elegirColor(robot);
                    juego.cambiarColor(color);
                    mensaje.append(" · color ").append(nombreColor(color));
                }
                if (juego.hayUnoPendiente() && juego.getJugadorQueDebeDecirUno() == robot) {
                    juego.decirUno();
                    mensaje.append(" · ¡UNO!");
                }
            } else {
                int cantidad = Math.max(1, juego.getPenalizacionAcumulada());
                juego.robarCarta();
                mensaje.append(robot.obtenerNombre()).append(" robó ").append(cantidad)
                        .append(cantidad == 1 ? " carta" : " cartas");
                error = anotarPenalizacionUno(mensaje);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mensaje.append("Error en el turno de ").append(robot.obtenerNombre()).append(": ").append(ex.getMessage());
            error = true;
        }
        tablero.mostrarToast(mensaje.toString(), error);
        despuesDeJugada();
    }


    public void robot() {
        detenerTemporizador();
        if (juego.hayGanador() || juego.getJugadorActual() == humano) {
            return;
        }
        temporizadorRobot = new Timer(2000, e -> jugarTurnoRobot());
        temporizadorRobot.setRepeats(false);
        temporizadorRobot.start();
    }

    private void detenerTemporizador() {
        if (temporizadorRobot != null) {
            temporizadorRobot.stop();
        }
    }

    // ------------------------------------------------------------------ fin de ronda y de partida

    private void despuesDeJugada() {
        refrescar();
        if (juego.hayGanador()) {
            terminarRonda();
        } else {
            robot();
        }
    }

    private void terminarRonda() {
        detenerTemporizador();
        if (juego.getGanadorPartida() != null) {
            guardarHistorial();
        }
        mostrarResultado();
    }


    public void guardarHistorial() {
        try {
            historial.registrarPartida(juego);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el historial: " + ex.getMessage(),
                    "Historial", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarHistorial() {
        List<PartidaDTO> partidas = historial.listar();
        StringBuilder texto = new StringBuilder();
        if (partidas.isEmpty()) {
            texto.append("Todavía no hay partidas guardadas.");
        } else {
            partidas.forEach(partida -> texto.append(partida).append("\n"));
            texto.append("\n").append(historial.estadisticas());
        }
        txtArea.setEditable(false);
        txtArea.setText(texto.toString());
        txtArea.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(txtArea), "Historial de partidas",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ------------------------------------------------------------------ textos y pantallas

    private boolean anotarPenalizacionUno(StringBuilder mensaje) {
        if (!juego.huboPenalizacionUno()) {
            return false;
        }
        Jugador olvidadizo = juego.getJugadorPenalizadoUno();
        mensaje.append(" · ").append(olvidadizo.obtenerNombre()).append(" no dijo UNO y roba 2 cartas");
        return olvidadizo == humano;
    }

    private String pedirColor() {
        String[] opciones = {"ROJO", "AMARILLO", "VERDE", "AZUL"};
        int eleccion = JOptionPane.showOptionDialog(this, "Elige el nuevo color", "Cambio de color",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        return eleccion >= 0 ? opciones[eleccion] : estrategia.elegirColor(humano);
    }

    private String rondas(int cantidad) {
        return cantidad + (cantidad == 1 ? " ronda" : " rondas");
    }

    private String nombreColor(String color) {
        return color.charAt(0) + color.substring(1).toLowerCase();
    }

    private String describir(Carta carta) {
        if (carta.esMasCuatro()) {
            return "Comodín +4";
        }
        if (carta.esCambioColor()) {
            return "Comodín de color";
        }
        String valor = carta.esMasDos() ? "+2" : carta.esSalta() ? "Salta"
                : carta.esReversa() ? "Reversa" : carta.obtenerValor();
        return nombreColor(carta.obtenerColor()) + " " + valor;
    }

    private void refrescar() {
        tablero.actualizar();
    }


    private void mostrarResultado() {
        boolean fin = juego.getGanadorPartida() != null;
        Jugador ganador = fin ? juego.getGanadorPartida() : juego.getGanador();
        boolean humanoGano = ganador == humano;
        ((JLabel) panelResultado.getClientProperty("tituloPuntos")).setText(fin ? "PUNTUACIÓN FINAL" : "PUNTUACIÓN");
        if (fin) {
            lblTurno.setText(humanoGano ? "CAMPEONA" : "FIN DE LA PARTIDA");
            lblMesa.setText(humanoGano ? "¡Victoria!" : "Derrota");
            lblMazo.setText(humanoGano
                    ? "Terminaste la partida en " + rondas(juego.getNumeroRonda())
                    : ganador.obtenerNombre() + " llegó a " + ganador.obtenerPuntos() + " puntos en "
                    + rondas(juego.getNumeroRonda()));
            btnSecundario.setText("Volver al menú");
            btnFin.setText("Jugar de nuevo");
        } else {
            lblTurno.setText("RONDA " + juego.getNumeroRonda());
            lblMesa.setText("Fin de la ronda");
            lblMazo.setText(ganador.obtenerNombre() + " suma " + juego.getPuntosGanadorRonda() + " puntos");
            btnSecundario.setText("Menú");
            btnFin.setText("Siguiente ronda");
        }
        List<Jugador> jugadores = juego.getJugadores();
        List<Jugador> ordenados = jugadores.stream()
                .sorted(Comparator.comparingInt(Jugador::obtenerPuntos).reversed()).toList();
        for (int i = 0; i < cantidadJugadoresVisibles; i++) {
            Jugador jugador = ordenados.get(i);
            etiquetas[i].setIcon(punto(TableroPanel.colorJugador(jugadores.indexOf(jugador))));
            etiquetas[i].setText(jugador.obtenerNombre());
            campos[i].setText(String.valueOf(jugador.obtenerPuntos()));
        }
        panelResultado.setVisible(true);
        panelResultado.revalidate();
        panelResultado.repaint();
    }

    private void construirPantallaJuego() {
        if (pantallaJuego != null) {
            contenedor.remove(pantallaJuego);
        }
        tablero = new TableroPanel(juego, humano, this);
        tablero.setAlignmentX(Component.CENTER_ALIGNMENT);
        tablero.setAlignmentY(Component.CENTER_ALIGNMENT);
        tablero.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panelResultado = construirResultado();
        panelResultado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelResultado.setAlignmentY(Component.CENTER_ALIGNMENT);
        panelResultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // La tarjeta de resultado se dibuja encima de la mesa: hay que avisarle a Swing que los
        // componentes se superponen, o la mesa se repinta sobre la tarjeta y esta desaparece.
        pantallaJuego = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        pantallaJuego.setLayout(new OverlayLayout(pantallaJuego));
        pantallaJuego.add(panelResultado);
        pantallaJuego.add(tablero);
        contenedor.add(pantallaJuego, "juego");
    }


    private JPanel construirResultado() {
        JPanel capa = new JPanel(new GridBagLayout());
        capa.setOpaque(false);
        capa.setVisible(false);

        JPanel tarjeta = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension tam = super.getPreferredSize();
                tam.width = 480;
                return tam;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fill(new RoundRectangle2D.Double(0, 10, getWidth(), getHeight() - 10, 34, 34));
                g2.setColor(new Color(12, 38, 29, 242));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() - 8, 34, 34));
                g2.setColor(new Color(255, 255, 255, 45));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 9, 34, 34));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(22, 32, 30, 32));

        lblTurno = texto("", new Color(246, 196, 32), Font.BOLD, 13);
        lblMesa = texto("", Color.WHITE, Font.BOLD, 40);
        lblMazo = texto("", new Color(190, 205, 198), Font.PLAIN, 15);
        tarjeta.add(lblTurno);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblMesa);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblMazo);
        tarjeta.add(Box.createVerticalStrut(16));

        campos = new JTextField[4];
        etiquetas = new JLabel[4];
        JPanel caja = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.setColor(new Color(255, 255, 255, 38));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, 18, 18));
                g2.dispose();
            }
        };
        caja.setOpaque(false);
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel tituloPuntos = texto("PUNTUACIÓN FINAL", new Color(205, 220, 212), Font.BOLD, 12);
        capa.putClientProperty("tituloPuntos", tituloPuntos);
        encabezado.add(tituloPuntos, BorderLayout.WEST);
        encabezado.add(texto("Meta 250", new Color(246, 196, 32), Font.BOLD, 12), BorderLayout.EAST);
        caja.add(encabezado);
        caja.add(Box.createVerticalStrut(10));
        for (int i = 0; i < cantidadJugadoresVisibles; i++) {
            etiquetas[i] = texto("", Color.WHITE, Font.PLAIN, 14);
            etiquetas[i].setIconTextGap(12);
            campos[i] = new JTextField("0", 4);
            campos[i].setEditable(false);
            campos[i].setOpaque(false);
            campos[i].setBorder(null);
            campos[i].setForeground(Color.WHITE);
            campos[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            campos[i].setHorizontalAlignment(JTextField.RIGHT);
            JPanel fila = new JPanel(new BorderLayout());
            fila.setOpaque(false);
            fila.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            fila.add(etiquetas[i], BorderLayout.WEST);
            fila.add(campos[i], BorderLayout.EAST);
            caja.add(fila);
        }
        caja.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.setMaximumSize(new Dimension(Integer.MAX_VALUE, caja.getPreferredSize().height));
        tarjeta.add(caja);
        tarjeta.add(Box.createVerticalStrut(18));

        btnSecundario = MenuPanel.crearBoton("Volver al menú", new Color(18, 52, 40), 15, true);
        btnFin = MenuPanel.crearBoton("Jugar de nuevo", new Color(229, 62, 52), 15, false);
        btnSecundario.addActionListener(e -> volverMenu());
        btnFin.addActionListener(e -> {
            if (juego.getGanadorPartida() != null) {
                iniciar(nombreHumano, robotsActuales);
            } else {
                botonFin();
            }
        });
        panelMano = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        panelMano.setOpaque(false);
        panelMano.add(btnSecundario);
        panelMano.add(btnFin);
        panelMano.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(panelMano);

        GridBagConstraints posicion = new GridBagConstraints();
        posicion.insets = new Insets(90, 0, 0, 0);
        capa.add(tarjeta, posicion);
        return capa;
    }

    private JLabel texto(String texto, Color color, int estilo, int tamano) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(color);
        etiqueta.setFont(new Font(Font.SANS_SERIF, estilo, tamano));
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return etiqueta;
    }


    private Icon punto(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y + 1, 10, 10);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
}

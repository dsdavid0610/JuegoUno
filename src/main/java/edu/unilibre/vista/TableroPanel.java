package edu.unilibre.vista;

import edu.unilibre.modelo.Carta;
import edu.unilibre.modelo.JuegoUno;
import edu.unilibre.modelo.Jugador;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;


public class TableroPanel extends JPanel {

    private final JuegoUno juego;
    private final AccionesTableroPanel acciones;
    private final Jugador humano;
    private List<Carta> cartasMano;

    private String mensajeToast;
    private boolean toastEsError;
    private Timer temporizadorToast;
    private boolean sentidoHorario = true;
    private Carta ultimaCartaVista;

    public TableroPanel(JuegoUno juego, Jugador humano, AccionesTableroPanel acciones) {
        if (juego == null || humano == null || acciones == null) {
            throw new IllegalArgumentException("El juego, el humano y las acciones son obligatorios");
        }
        this.juego = juego;
        this.humano = humano;
        this.acciones = acciones;
        this.cartasMano = new ArrayList<>(humano.obtenerMano());
        this.ultimaCartaVista = juego.getCartaSuperior();
        setBackground(new Color(21, 80, 55));
        setPreferredSize(new Dimension(1200, 800));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alHacerClic(e.getX(), e.getY());
            }
        });
    }


    public static Color colorJugador(int puesto) {
        Color[] colores = {new Color(239, 83, 80), new Color(66, 133, 244), new Color(52, 168, 83), new Color(251, 160, 30)};
        return colores[Math.floorMod(puesto, colores.length)];
    }


    public void reiniciarVista() {
        mensajeToast = null;
        if (temporizadorToast != null) {
            temporizadorToast.stop();
        }
        sentidoHorario = true;
        ultimaCartaVista = juego.getCartaSuperior();
        actualizar();
    }

    public boolean esTurnoHumano() {
        return juego.cantidadJugadores() > 0
                && !juego.hayGanador()
                && juego.getCartaSuperior() != null
                && juego.getJugadorActual() == humano;
    }

    /** Vuelve a leer la mano del humano y repinta. */
    public void actualizar() {
        cartasMano = new ArrayList<>(humano.obtenerMano());
        Carta tope = juego.getCartaSuperior();
        if (tope != null && tope != ultimaCartaVista) {
            if (ultimaCartaVista != null && tope.esReversa() && juego.cantidadJugadores() > 2) {
                sentidoHorario = !sentidoHorario;
            }
            ultimaCartaVista = tope;
        }
        repaint();
    }


    public void mostrarToast(String mensaje, boolean error) {
        mensajeToast = mensaje;
        toastEsError = error;
        if (temporizadorToast != null) {
            temporizadorToast.stop();
        }
        temporizadorToast = new Timer(4000, e -> {
            mensajeToast = null;
            repaint();
        });
        temporizadorToast.setRepeats(false);
        temporizadorToast.start();
        repaint();
    }

    // ------------------------------------------------------------------ geometría (lienzo virtual)

    private double escala() {
        double s = Math.min(getWidth() / (double) 1200, getHeight() / (double) 800);
        return s > 0 ? s : 1;
    }

    private double desvioX() {
        return (getWidth() - 1200 * escala()) / 2;
    }

    private double desvioY() {
        return (getHeight() - 800 * escala()) / 2;
    }

    private Point2D aVirtual(int x, int y) {
        return new Point2D.Double((x - desvioX()) / escala(), (y - desvioY()) / escala());
    }

    private Rectangle aReal(Rectangle2D r) {
        double s = escala();
        return new Rectangle((int) Math.round(desvioX() + r.getX() * s), (int) Math.round(desvioY() + r.getY() * s),
                (int) Math.round(r.getWidth() * s), (int) Math.round(r.getHeight() * s));
    }

    private Rectangle2D areaMenu() {
        return new Rectangle2D.Double(1098, 16, 86, 34);
    }

    private Rectangle2D areaMazo() {
        return new Rectangle2D.Double(392, 262, 126, 240);
    }

    private Ellipse2D botonUno() {
        return new Ellipse2D.Double(1118 - 46, 692 - 46, 92, 92);
    }

    private double pasoMano() {
        int n = cartasMano.size();
        double paso = 100 * 0.95;
        if (n > 1) {
            paso = Math.min(paso, (880.0 - 100) / (n - 1));
        }
        return paso;
    }

    private boolean levantada(int i) {
        return esJugable(cartasMano.get(i));
    }

    private AffineTransform transformada(int i) {
        int n = cartasMano.size();
        double d = i - (n - 1) / 2.0;
        double grados = d * Math.min(3.2, 26.0 / Math.max(n, 1));
        double x = 600 + d * pasoMano();
        double y = 684 + Math.min(1.2 * d * d, 22) - (levantada(i) ? 24 : 0);
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.rotate(Math.toRadians(grados));
        return at;
    }

    private Shape formaCarta(int i) {
        Shape s = new RoundRectangle2D.Double(-100 / 2.0, -146 / 2.0,
                100, 146, 18, 18);
        return transformada(i).createTransformedShape(s);
    }


    Rectangle areaCarta(int indice) {
        return aReal(formaCarta(indice).getBounds2D());
    }


    java.awt.Point puntoVisible(int indice) {
        int n = cartasMano.size();
        double lx = indice == n - 1 ? 0 : -100 / 2.0 + Math.min(pasoMano(), 100) / 2.0;
        Point2D p = transformada(indice).transform(new Point2D.Double(lx, 0), null);
        return new java.awt.Point((int) Math.round(desvioX() + p.getX() * escala()),
                (int) Math.round(desvioY() + p.getY() * escala()));
    }


    Carta cartaEnPunto(int x, int y) {
        Point2D p = aVirtual(x, y);
        for (int i = cartasMano.size() - 1; i >= 0; i--) {
            if (formaCarta(i).contains(p)) {
                return cartasMano.get(i);
            }
        }
        return null;
    }

    private boolean esJugable(Carta carta) {
        if (!esTurnoHumano()) {
            return false;
        }
        if (juego.getPenalizacionAcumulada() > 0) {
            return carta.esMasDos() || carta.esMasCuatro();
        }
        if (!carta.sePuedeJugarSobre(juego.getCartaSuperior(), juego.getColorActual())) {
            return false;
        }
        return !carta.esMasCuatro() || !humano.tieneCartaDeColor(juego.getColorActual());
    }

    // ------------------------------------------------------------------ interacción

    private void alHacerClic(int x, int y) {
        Point2D p = aVirtual(x, y);
        if (areaMenu().contains(p)) {
            acciones.volverMenu();
            return;
        }
        if (juego.cantidadJugadores() == 0 || juego.hayGanador()) {
            return;
        }
        if (botonUno().contains(p)) {
            acciones.decirUno();
            return;
        }
        if (areaMazo().contains(p)) {
            acciones.robar();
            return;
        }
        Carta carta = cartaEnPunto(x, y);
        if (carta == null) {
            return;
        }
        if (!esTurnoHumano()) {
            mostrarToast("Todavía no es tu turno", true);
            return;
        }
        acciones.jugar(carta);
    }

    // ------------------------------------------------------------------ pintado

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setColor(new Color(14, 60, 42));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.translate(desvioX(), desvioY());
        g2.scale(escala(), escala());

        dibujarFondo(g2);
        if (juego.cantidadJugadores() > 0 && juego.getCartaSuperior() != null) {
            if (juego.hayGanador()) {
                dibujarFinDeRonda(g2);
            } else {
                dibujarMarcador(g2);
                dibujarRivales(g2);
                dibujarCentro(g2);
                dibujarMano(g2);
                dibujarHumano(g2);
                dibujarBotonUno(g2);
            }
        }
        dibujarBotonMenu(g2);
        dibujarToast(g2);
        g2.dispose();
    }

    private void dibujarFondo(Graphics2D g2) {
        g2.setPaint(new RadialGradientPaint(600f, 360f, 760f, new float[]{0f, 1f},
                new Color[]{new Color(46, 134, 91), new Color(20, 78, 53)}));
        g2.fill(new RoundRectangle2D.Double(0, 0, 1200, 800, 40, 40));
        g2.setColor(new Color(14, 64, 44));
        g2.setStroke(new BasicStroke(12f));
        g2.draw(new RoundRectangle2D.Double(6, 6, 1200 - 12, 800 - 12, 36, 36));
    }

    private void dibujarMarcador(Graphics2D g2) {
        List<Jugador> jugadores = juego.getJugadores();
        int alto = 40 + jugadores.size() * 20;
        g2.setColor(new Color(0, 0, 0, 105));
        g2.fill(new RoundRectangle2D.Double(14, 14, 178, alto, 16, 16));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("Ronda " + juego.getNumeroRonda() + "  ·  Meta 250", 26, 38);
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            boolean enTurno = juego.getJugadorActual() == j;
            int y = 62 + i * 20;
            g2.setColor(colorJugador(i));
            g2.fillOval(26, y - 10, 10, 10);
            g2.setFont(new Font(Font.SANS_SERIF, enTurno ? Font.BOLD : Font.PLAIN, 13));
            g2.setColor(enTurno ? new Color(246, 196, 32) : new Color(230, 240, 235));
            g2.drawString(j.obtenerNombre(), 44, y);
            String puntos = String.valueOf(j.obtenerPuntos());
            g2.drawString(puntos, 180 - g2.getFontMetrics().stringWidth(puntos), y);
        }
    }

    private double[] posicionesRivales(int n) {
        return switch (n) {
            case 1 -> new double[]{687};
            case 2 -> new double[]{520, 860};
            default -> new double[]{365, 687, 1010};
        };
    }

    private void dibujarRivales(Graphics2D g2) {
        List<Jugador> jugadores = juego.getJugadores();
        List<Jugador> rivales = jugadores.stream().filter(j -> j != humano).toList();
        double[] xs = posicionesRivales(rivales.size());
        for (int k = 0; k < rivales.size(); k++) {
            Jugador rival = rivales.get(k);
            double cx = xs[Math.min(k, xs.length - 1)];
            boolean enTurno = juego.getJugadorActual() == rival;
            int puesto = jugadores.indexOf(rival);

            int visibles = Math.min(rival.cantidadCartas(), 7);
            for (int c = 0; c < visibles; c++) {
                Graphics2D gc = (Graphics2D) g2.create();
                double d = c - (visibles - 1) / 2.0;
                gc.translate(cx + d * 7, 112);
                gc.rotate(Math.toRadians(d * 6));
                gc.scale(0.40, 0.40);
                DibujoCartas.dibujarCartas(gc, -100 / 2, -146 / 2, null, null);
                gc.dispose();
            }
            avatar(g2, cx, 68, 25, puesto, rival.obtenerNombre(), enTurno);
            insignia(g2, cx + 26, 38, rival.cantidadCartas());
            pildora(g2, rival.obtenerNombre(), cx, 160, 13, Color.WHITE);
        }
    }

    private void avatar(Graphics2D g2, double cx, double cy, double r, int puesto, String nombre, boolean enTurno) {
        if (enTurno) {
            g2.setPaint(new RadialGradientPaint((float) cx, (float) cy, (float) (r + 22), new float[]{0.55f, 1f},
                    new Color[]{new Color(220, 205, 70, 170), new Color(220, 205, 70, 0)}));
            g2.fill(new Ellipse2D.Double(cx - r - 22, cy - r - 22, 2 * (r + 22), 2 * (r + 22)));
            g2.setColor(new Color(214, 198, 52, 230));
            g2.setStroke(new BasicStroke(6f));
            g2.draw(new Ellipse2D.Double(cx - r - 6, cy - r - 6, 2 * (r + 6), 2 * (r + 6)));
        }
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fill(new Ellipse2D.Double(cx - r + 2, cy - r + 3, 2 * r, 2 * r));
        g2.setColor(colorJugador(puesto));
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g2.setColor(new Color(255, 255, 255, 190));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        String inicial = nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase();
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, (int) (r * 1.0)));
        centrado(g2, inicial, cx, cy, Color.WHITE);
    }

    private void insignia(Graphics2D g2, double cx, double cy, int cantidad) {
        g2.setColor(new Color(20, 20, 22));
        g2.fill(new Ellipse2D.Double(cx - 11, cy - 11, 22, 22));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(cx - 11, cy - 11, 22, 22));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        centrado(g2, String.valueOf(cantidad), cx, cy, Color.WHITE);
    }

    private void pildora(Graphics2D g2, String texto, double cx, double cy, int tamano, Color color) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamano));
        FontMetrics fm = g2.getFontMetrics();
        double ancho = fm.stringWidth(texto) + 26;
        double alto = tamano + 14;
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fill(new RoundRectangle2D.Double(cx - ancho / 2, cy - alto / 2, ancho, alto, alto, alto));
        centrado(g2, texto, cx, cy, color);
    }

    private void dibujarCentro(Graphics2D g2) {
        // mazo
        for (int i = 2; i >= 0; i--) {
            DibujoCartas.dibujarCartas(g2, 405 + i * 3, 280 - i * 3, null, null);
        }
        boolean sinJugada = esTurnoHumano() && !humano.puedeJugar(juego.getCartaSuperior(), juego.getColorActual())
                && juego.getPenalizacionAcumulada() == 0;
        pildora(g2, "ROBAR", 455, 462, 13, sinJugada ? new Color(255, 232, 90) : new Color(238, 214, 90));
        g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        centrado(g2, juego.cartasEnMazo() + (juego.cartasEnMazo() == 1 ? " carta en el mazo" : " en el mazo"), 455, 488, new Color(205, 220, 212));

        // anillo de color y sentido del juego
        double cx = 667;
        double cy = 355;
        Color activo = DibujoCartas.colorDe(juego.getColorActual());
        g2.setPaint(new RadialGradientPaint((float) cx, (float) cy, 200f, new float[]{0.45f, 1f},
                new Color[]{new Color(activo.getRed(), activo.getGreen(), activo.getBlue(), 120),
                        new Color(activo.getRed(), activo.getGreen(), activo.getBlue(), 0)}));
        g2.fill(new Ellipse2D.Double(cx - 200, cy - 200, 400, 400));
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fill(new Ellipse2D.Double(cx - 128, cy - 128, 256, 256));
        g2.setColor(activo);
        g2.setStroke(new BasicStroke(9f));
        g2.draw(new Ellipse2D.Double(cx - 133, cy - 133, 266, 266));
        g2.setColor(new Color(215, 225, 220, 215));
        if (sentidoHorario) {
            flechaCircular(g2, cx, cy, 158, 200, 285);
            flechaCircular(g2, cx, cy, 158, 20, 105);
        } else {
            flechaCircular(g2, cx, cy, 158, 285, 200);
            flechaCircular(g2, cx, cy, 158, 105, 20);
        }

        // carta de la mesa
        Carta tope = juego.getCartaSuperior();
        Graphics2D gc = (Graphics2D) g2.create();
        gc.translate(cx, cy);
        gc.rotate(Math.toRadians(-5));
        gc.scale(1.12, 1.12);
        DibujoCartas.dibujarCartas(gc, -100 / 2, -146 / 2,
                tope.obtenerColor(), tope.obtenerValor());
        gc.dispose();
    }

    private void flechaCircular(Graphics2D g2, double cx, double cy, double r, double desde, double hasta) {
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D arco = new Path2D.Double();
        int pasos = 40;
        for (int i = 0; i <= pasos; i++) {
            double a = Math.toRadians(desde + (hasta - desde) * i / pasos);
            double px = cx + r * Math.cos(a);
            double py = cy + r * Math.sin(a);
            if (i == 0) {
                arco.moveTo(px, py);
            } else {
                arco.lineTo(px, py);
            }
        }
        g2.draw(arco);
        double sentido = Math.signum(hasta - desde);
        double a = Math.toRadians(hasta);
        double px = cx + r * Math.cos(a);
        double py = cy + r * Math.sin(a);
        double tx = -Math.sin(a) * sentido;
        double ty = Math.cos(a) * sentido;
        double nx = -ty;
        double ny = tx;
        Path2D punta = new Path2D.Double();
        punta.moveTo(px + tx * 13, py + ty * 13);
        punta.lineTo(px - tx * 4 + nx * 10, py - ty * 4 + ny * 10);
        punta.lineTo(px - tx * 4 - nx * 10, py - ty * 4 - ny * 10);
        punta.closePath();
        g2.fill(punta);
    }

    private void dibujarMano(Graphics2D g2) {
        for (int i = 0; i < cartasMano.size(); i++) {
            Carta carta = cartasMano.get(i);
            Graphics2D gc = (Graphics2D) g2.create();
            gc.transform(transformada(i));
            DibujoCartas.dibujarCartas(gc, -100 / 2, -146 / 2,
                    carta.obtenerColor(), carta.obtenerValor());
            gc.dispose();
        }
    }

    private void dibujarHumano(Graphics2D g2) {
        int puesto = juego.getJugadores().indexOf(humano);
        boolean enTurno = esTurnoHumano();
        avatar(g2, 66, 682, 33, puesto, humano.obtenerNombre(), enTurno);
        pildora(g2, humano.obtenerNombre(), 66, 738, 13, Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        String turno = enTurno ? "¡Es tu turno!" : "Turno: " + juego.getJugadorActual().obtenerNombre();
        centrado(g2, turno, 66, 764, new Color(205, 220, 212));
    }

    private void dibujarBotonUno(Graphics2D g2) {
        double cx = 1118;
        double cy = 692;
        boolean pendiente = juego.hayUnoPendiente() && juego.getJugadorQueDebeDecirUno() == humano;
        if (pendiente) {
            g2.setColor(new Color(255, 230, 90, 140));
            g2.fill(new Ellipse2D.Double(cx - 62, cy - 62, 124, 124));
        }
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fill(new Ellipse2D.Double(cx - 46 + 3, cy - 46 + 5, 92, 92));
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(cx - 46, cy - 46, 92, 92));
        g2.setColor(new Color(229, 46, 51));
        g2.fill(new Ellipse2D.Double(cx - 40, cy - 40, 80, 80));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 30));
        centrado(g2, "UNO!", cx + 2, cy + 3, new Color(120, 12, 18));
        centrado(g2, "UNO!", cx, cy, new Color(253, 197, 22));
    }

    private void dibujarBotonMenu(Graphics2D g2) {
        Rectangle2D r = areaMenu();
        g2.setColor(new Color(10, 50, 36, 220));
        g2.fill(new RoundRectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 14, 14));
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 14, 14));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 15));
        centrado(g2, "Menú", r.getCenterX(), r.getCenterY(), Color.WHITE);
    }

    private void dibujarToast(Graphics2D g2) {
        if (mensajeToast == null || juego.hayGanador()) {
            return;
        }
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        double ancho = fm.stringWidth(mensajeToast) + 52;
        double x = 600 - ancho / 2;
        g2.setColor(toastEsError ? new Color(160, 30, 35, 235) : new Color(14, 36, 30, 235));
        g2.fill(new RoundRectangle2D.Double(x, 527, ancho, 38, 10, 10));
        centrado(g2, mensajeToast, 600, 546, Color.WHITE);
    }

    // ------------------------------------------------------------------ fin de ronda

    private void dibujarFinDeRonda(Graphics2D g2) {
        Jugador ganador = juego.getGanador();
        boolean fin = juego.getGanadorPartida() != null;
        String que = fin ? "partida" : "ronda";
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(new Color(246, 196, 32));
        g2.drawString(fin ? "R E S U L T A D O   F I N A L" : "F I N   D E   L A   R O N D A  " + juego.getNumeroRonda(), 48, 54);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        g2.setColor(Color.WHITE);
        g2.drawString("¡" + (ganador == null ? "Nadie" : ganador.obtenerNombre()) + " ganó la " + que + "!", 48, 90);

        List<Jugador> jugadores = juego.getJugadores();
        List<Jugador> rivales = jugadores.stream().filter(j -> j != humano).toList();
        double[] xs = posicionesRivales(rivales.size());
        for (int k = 0; k < rivales.size(); k++) {
            Jugador rival = rivales.get(k);
            resumenFinal(g2, rival, jugadores.indexOf(rival), xs[Math.min(k, xs.length - 1)], 142, 25, rival == ganador, que);
        }
        resumenFinal(g2, humano, jugadores.indexOf(humano), 100, 682, 33, humano == ganador, que);
    }

    private void resumenFinal(Graphics2D g2, Jugador j, int puesto, double cx, double cy, double r,
                              boolean gana, String que) {
        avatar(g2, cx, cy, r, puesto, j.obtenerNombre(), gana);
        pildora(g2, j.obtenerNombre(), cx, cy + r + 24, 13, Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        String texto = gana ? "Ganador de la " + que
                : j.cantidadCartas() + (j.cantidadCartas() == 1 ? " carta" : " cartas");
        centrado(g2, texto, cx, cy + r + 48, gana ? new Color(246, 196, 32) : new Color(205, 220, 212));
    }

    private void centrado(Graphics2D g2, String texto, double cx, double cy, Color color) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(color);
        g2.drawString(texto, (float) (cx - fm.stringWidth(texto) / 2.0),
                (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }
}

package edu.unilibre.vista;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class MenuPanel extends JPanel {

    private final InicioMenuPanel inicio;
    private final Runnable alVerHistorial;
    private final JTextField campoNombre;
    private final JLabel lblError;
    private final JLabel lblTitulo;
    private final JTextField[] camposRobots;
    private final JPanel[] filasRobots;
    private int cantidadRobots;
    private JPanel tarjeta;

    public MenuPanel(InicioMenuPanel inicio) {
        this(inicio, null);
    }

    public MenuPanel(InicioMenuPanel inicio, Runnable alVerHistorial) {
        if (inicio == null) {
            throw new IllegalArgumentException("El controlador del menú es obligatorio");
        }
        this.inicio = inicio;
        this.alVerHistorial = alVerHistorial;
        this.campoNombre = new JTextField("Jugador");
        this.lblError = new JLabel(" ");
        this.lblTitulo = new JLabel();
        this.camposRobots = new JTextField[3];
        this.filasRobots = new JPanel[3];
        this.cantidadRobots = 3;
        preparar();
    }

    /** Construye los componentes del menú. */
    public void preparar() {
        removeAll();
        setLayout(null);
        setOpaque(true);
        setPreferredSize(new Dimension(1200, 800));

        tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Double(3, 6, getWidth() - 6, getHeight() - 6, 26, 26));
                g2.setColor(new Color(246, 248, 247));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 3, getHeight() - 6, 26, 26));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(22, 28, 30, 31));

        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitulo.setForeground(new Color(30, 30, 34));
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        JButton menos = crearBoton("−", new Color(66, 133, 244), 14, false);
        JButton mas = crearBoton("+", new Color(66, 133, 244), 14, false);
        menos.setBorder(BorderFactory.createEmptyBorder(5, 14, 9, 14));
        mas.setBorder(BorderFactory.createEmptyBorder(5, 14, 9, 14));
        menos.setToolTipText("Un robot menos");
        mas.setToolTipText("Un robot más");
        menos.addActionListener(e -> cambiarRobots(-1));
        mas.addActionListener(e -> cambiarRobots(1));
        JPanel filaTitulo = new JPanel(new java.awt.BorderLayout(8, 0));
        filaTitulo.setOpaque(false);
        filaTitulo.add(menos, java.awt.BorderLayout.WEST);
        filaTitulo.add(lblTitulo, java.awt.BorderLayout.CENTER);
        filaTitulo.add(mas, java.awt.BorderLayout.EAST);
        filaTitulo.setMaximumSize(new Dimension(320, 40));
        tarjeta.add(filaTitulo);
        tarjeta.add(Box.createVerticalStrut(12));

        estiloCampo(campoNombre, true);
        campoNombre.setHorizontalAlignment(JTextField.LEFT);
        tarjeta.add(fila(1, campoNombre));
        for (int i = 0; i < 3; i++) {
            camposRobots[i] = new JTextField("Robot " + (i + 1));
            estiloCampo(camposRobots[i], false);
            filasRobots[i] = fila(i + 2, camposRobots[i]);
            tarjeta.add(Box.createVerticalStrut(8));
            tarjeta.add(filasRobots[i]);
        }

        lblError.setForeground(new Color(200, 40, 40));
        lblError.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblError.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblError);
        tarjeta.add(Box.createVerticalStrut(6));

        JButton jugar = crearBoton("¡JUGAR!", new Color(52, 168, 83), 22, false);
        jugar.setAlignmentX(CENTER_ALIGNMENT);
        jugar.addActionListener(e -> arrancar());
        campoNombre.addActionListener(e -> arrancar());
        tarjeta.add(jugar);
        tarjeta.add(Box.createVerticalStrut(12));

        JButton historial = crearBoton("Historial de partidas", new Color(66, 133, 244), 14, false);
        historial.setAlignmentX(CENTER_ALIGNMENT);
        historial.addActionListener(e -> {
            if (alVerHistorial != null) {
                alVerHistorial.run();
            }
        });
        tarjeta.add(historial);

        add(tarjeta);
        actualizarRobots();
        revalidate();
        repaint();
    }

    private void estiloCampo(JTextField campo, boolean editable) {
        campo.setEditable(editable);
        campo.setEnabled(editable);
        campo.setDisabledTextColor(new Color(70, 70, 76));
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        campo.setBackground(editable ? Color.WHITE : new Color(238, 239, 241));
        campo.setForeground(new Color(40, 40, 44));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 202, 206)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        campo.setPreferredSize(new Dimension(232, 34));
    }

    private JPanel fila(int numero, JTextField campo) {
        JPanel fila = new JPanel(new java.awt.BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.add(insignia(numero), java.awt.BorderLayout.WEST);
        fila.add(campo, java.awt.BorderLayout.CENTER);
        fila.setMaximumSize(new Dimension(290, 36));
        return fila;
    }

    public void mostrarError(String mensaje) {
        lblError.setText(mensaje == null || mensaje.isBlank() ? " " : mensaje);
    }


    public void cambiarRobots(int delta) {
        cantidadRobots = Math.max(1, Math.min(3, cantidadRobots + delta));
        actualizarRobots();
    }


    public void arrancar() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("Escribe tu nombre para comenzar");
            return;
        }
        mostrarError(null);
        inicio.iniciar(nombre, cantidadRobots);
    }

    private void actualizarRobots() {
        lblTitulo.setText("Tú contra " + cantidadRobots + (cantidadRobots == 1 ? " robot" : " robots"));
        for (int i = 0; i < 3; i++) {
            filasRobots[i].setVisible(i < cantidadRobots);
        }
        if (tarjeta != null) {
            tarjeta.revalidate();
            revalidate();
            repaint();
        }
    }

    public int getCantidadRobots() {
        return cantidadRobots;
    }

    public JTextField getCampoNombre() {
        return campoNombre;
    }

    public JLabel getLblError() {
        return lblError;
    }

    // ------------------------------------------------------------------ pintado

    @Override
    public void doLayout() {
        if (tarjeta == null) {
            return;
        }
        Dimension tam = tarjeta.getPreferredSize();
        tam.width = Math.max(tam.width, 380);
        double escala = escala();
        int x = (getWidth() - tam.width) / 2;
        int y = (int) Math.round(desvioY() + 800 * 0.375 * escala);
        tarjeta.setBounds(x, y, tam.width, tam.height);
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(21, 80, 55));
        g2.fillRect(0, 0, getWidth(), getHeight());
        float radio = (float) Math.max(getWidth(), getHeight()) * 0.7f;
        g2.setPaint(new RadialGradientPaint(getWidth() / 2f, getHeight() * 0.42f, radio,
                new float[]{0f, 1f}, new Color[]{new Color(46, 134, 91), new Color(21, 80, 55)}));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.translate(desvioX(), desvioY());
        g2.scale(escala(), escala());

        carta(g2, 120, 240, -20, "ROJO", "7");
        carta(g2, 205, 462, -12, "AZUL", "MAS_DOS");
        carta(g2, 95, 642, -8, "AMARILLO", "SALTO");
        carta(g2, 1080, 220, 15, "CAMBIO_COLOR", "MAS_CUATRO");
        carta(g2, 995, 455, 10, "VERDE", "REVERSA");
        carta(g2, 1105, 648, 10, "CAMBIO_COLOR", "CAMBIO_COLOR");
        logo(g2);
        g2.dispose();
    }

    private void carta(Graphics2D g2, double cx, double cy, double grados, String color, String valor) {
        Graphics2D gc = (Graphics2D) g2.create();
        gc.translate(cx, cy);
        gc.rotate(Math.toRadians(grados));
        gc.scale(1.28, 1.28);
        DibujoCartas.dibujarCartas(gc, -100 / 2, -146 / 2, color, valor);
        gc.dispose();
    }

    private void logo(Graphics2D g2) {
        Graphics2D gl = (Graphics2D) g2.create();
        gl.translate(600, 205);
        gl.rotate(Math.toRadians(-6));
        gl.setColor(new Color(0, 0, 0, 70));
        gl.fill(new Ellipse2D.Double(-157 + 6, -75 + 9, 314, 150));
        gl.setColor(Color.WHITE);
        gl.fill(new Ellipse2D.Double(-157, -75, 314, 150));
        gl.setColor(new Color(229, 46, 51));
        gl.fill(new Ellipse2D.Double(-150, -68, 300, 136));
        gl.setColor(new Color(150, 20, 28));
        gl.setStroke(new BasicStroke(3f));
        gl.draw(new Ellipse2D.Double(-150, -68, 300, 136));
        gl.rotate(Math.toRadians(-4));
        Font fuente = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 110);
        gl.setFont(fuente);
        FontMetrics fm = gl.getFontMetrics();
        int ancho = fm.stringWidth("UNO");
        float tx = -ancho / 2f;
        float ty = (fm.getAscent() - fm.getDescent()) / 2f;
        gl.setColor(new Color(130, 15, 22));
        gl.drawString("UNO", tx + 5, ty + 6);
        gl.setColor(new Color(253, 197, 22));
        gl.drawString("UNO", tx, ty);
        gl.dispose();
    }

    // ------------------------------------------------------------------ componentes propios


    private JComponent insignia(int numero) {
        JComponent insignia = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Color[] colores = {new Color(229, 83, 83), new Color(66, 133, 244), new Color(52, 168, 83), new Color(251, 160, 30)};
                g2.setColor(colores[(numero - 1) % colores.length]);
                g2.fillOval(0, 1, 30, 30);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 15));
                FontMetrics fm = g2.getFontMetrics();
                String texto = String.valueOf(numero);
                g2.drawString(texto, (30 - fm.stringWidth(texto)) / 2f, 1 + (30 + fm.getAscent() - fm.getDescent()) / 2f);
                g2.dispose();
            }
        };
        insignia.setPreferredSize(new Dimension(32, 32));
        return insignia;
    }


    public static JButton crearBoton(String texto, Color fondo, int tamano, boolean contorno) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight() - 4;
                int arco = h;
                Color color = getModel().isPressed() ? fondo.darker()
                        : getModel().isRollover() ? fondo.brighter() : fondo;
                if (!contorno) {
                    g2.setColor(new Color(0, 0, 0, 70));
                    g2.fill(new RoundRectangle2D.Double(0, 4, w, h, arco, arco));
                }
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arco, arco));
                if (contorno) {
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.draw(new RoundRectangle2D.Double(0.7, 0.7, w - 1.4, h - 1.4, arco, arco));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamano));
        boton.setForeground(Color.WHITE);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(tamano / 2 + 2, tamano + 8, tamano / 2 + 6, tamano + 8));
        return boton;
    }
}

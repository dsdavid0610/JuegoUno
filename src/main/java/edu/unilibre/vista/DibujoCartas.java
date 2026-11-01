package edu.unilibre.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;


public final class DibujoCartas {

    private Graphics2D g;

    private DibujoCartas() {
    }

    public static Color colorDe(String color) {
        if (color == null) {
            return new Color(28, 28, 32);
        }
        return switch (color) {
            case "ROJO" -> new Color(229, 46, 51);
            case "AMARILLO" -> new Color(253, 197, 22);
            case "VERDE" -> new Color(52, 160, 72);
            case "AZUL" -> new Color(22, 104, 200);
            default -> new Color(28, 28, 32);
        };
    }

    public static String etiqueta(String valor) {
        if (valor == null) {
            return "UNO";
        }
        return switch (valor) {
            case "MAS_DOS" -> "+2";
            case "MAS_CUATRO" -> "+4";
            case "SALTO" -> "\u2298";
            case "REVERSA" -> "\u21C4";
            case "CAMBIO_COLOR" -> "\u2605";
            default -> valor;
        };
    }

    public static void dibujarCartas(Graphics2D g, int x, int y, String color, String valor) {
        DibujoCartas dibujo = new DibujoCartas();
        dibujo.g = g;
        dibujo.dibujarCartas(x, y, color, valor);
    }

    private void dibujarCartas(int x, int y, String color, String valor) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        boolean reverso = color == null;
        boolean comodin = "CAMBIO_COLOR".equals(color);
        Color base = (reverso || comodin) ? colorDe("NEGRO") : colorDe(color);

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 5, 100, 146, 18, 18));
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(x, y, 100, 146, 18, 18));
        g2.setColor(base);
        g2.fill(new RoundRectangle2D.Double(x + 5, y + 5, 100 - 10, 146 - 10, 13, 13));

        double cx = x + 100 / 2.0;
        double cy = y + 146 / 2.0;
        Shape elipse = AffineTransform.getRotateInstance(Math.toRadians(22), cx, cy)
                .createTransformedShape(new Ellipse2D.Double(cx - 31, cy - 49, 62, 98));

        if (reverso) {
            g2.setColor(colorDe("ROJO"));
            g2.fill(elipse);
            Graphics2D gt = (Graphics2D) g2.create();
            gt.rotate(-Math.toRadians(22), cx, cy);
            texto(gt, "UNO", cx + 2, cy + 3, new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 30), new Color(110, 10, 15));
            texto(gt, "UNO", cx, cy, new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 30), colorDe("AMARILLO"));
            gt.dispose();
        } else if (comodin) {
            dibujarElipseDeColores(g2, elipse, cx, cy);
            if ("MAS_CUATRO".equals(valor)) {
                Font fuente = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 44);
                texto(g2, "+4", cx + 2, cy + 3, fuente, new Color(0, 0, 0, 120));
                texto(g2, "+4", cx, cy, fuente, Color.WHITE);
            }
            esquinas(g2, x, y, valor, color);
        } else {
            g2.setColor(Color.WHITE);
            g2.fill(elipse);
            centro(g2, valor, cx, cy, base);
            esquinas(g2, x, y, valor, color);
        }
        g2.dispose();
    }

    private void dibujarElipseDeColores(Graphics2D g2, Shape elipse, double cx, double cy) {
        Graphics2D gc = (Graphics2D) g2.create();
        gc.clip(elipse);
        gc.rotate(Math.toRadians(22), cx, cy);
        gc.setColor(colorDe("ROJO"));
        gc.fill(new java.awt.geom.Rectangle2D.Double(cx - 60, cy - 60, 60, 60));
        gc.setColor(colorDe("AZUL"));
        gc.fill(new java.awt.geom.Rectangle2D.Double(cx, cy - 60, 60, 60));
        gc.setColor(colorDe("AMARILLO"));
        gc.fill(new java.awt.geom.Rectangle2D.Double(cx - 60, cy, 60, 60));
        gc.setColor(colorDe("VERDE"));
        gc.fill(new java.awt.geom.Rectangle2D.Double(cx, cy, 60, 60));
        gc.dispose();
    }

    private void centro(Graphics2D g2, String valor, double cx, double cy, Color color) {
        if ("SALTO".equals(valor)) {
            simboloSalto(g2, cx, cy, 27, color);
        } else if ("REVERSA".equals(valor)) {
            simboloReversa(g2, cx, cy, 27, color);
        } else {
            String texto = "MAS_DOS".equals(valor) ? "+2" : valor;
            Font fuente = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, "MAS_DOS".equals(valor) ? 44 : 60);
            texto(g2, texto, cx + 2, cy + 3, fuente, new Color(120, 120, 120, 150));
            texto(g2, texto, cx, cy, fuente, color);
            if ("6".equals(valor) || "9".equals(valor)) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int) cx - 14, (int) cy + 28, (int) cx + 14, (int) cy + 28);
            }
        }
    }

    private void esquinas(Graphics2D g2, int x, int y, String valor, String color) {
        for (int vuelta = 0; vuelta < 2; vuelta++) {
            Graphics2D gc = (Graphics2D) g2.create();
            if (vuelta == 1) {
                gc.rotate(Math.PI, x + 100 / 2.0, y + 146 / 2.0);
            }
            double ex = x + 17;
            double ey = y + 20;
            if ("SALTO".equals(valor)) {
                simboloSalto(gc, ex, ey, 8, Color.WHITE);
            } else if ("REVERSA".equals(valor)) {
                simboloReversa(gc, ex, ey, 9, Color.WHITE);
            } else if ("CAMBIO_COLOR".equals(valor)) {
                Shape punto = new Ellipse2D.Double(ex - 8, ey - 8, 16, 16);
                dibujarElipseDeColores(gc, punto, ex, ey);
            } else {
                String texto = "MAS_DOS".equals(valor) ? "+2" : "MAS_CUATRO".equals(valor) ? "+4" : valor;
                texto(gc, texto, ex, ey, new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 17), Color.WHITE);
            }
            gc.dispose();
        }
    }

    private void simboloSalto(Graphics2D g2, double cx, double cy, double r, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke((float) (r * 0.3), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g2.drawLine((int) Math.round(cx - r * 0.7), (int) Math.round(cy - r * 0.7),
                (int) Math.round(cx + r * 0.7), (int) Math.round(cy + r * 0.7));
    }

    private void simboloReversa(Graphics2D g2, double cx, double cy, double r, Color color) {
        g2.setColor(color);
        float grosor = (float) (r * 0.26);
        g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double dy = r * 0.42;
        g2.drawLine((int) Math.round(cx - r), (int) Math.round(cy - dy), (int) Math.round(cx + r * 0.45), (int) Math.round(cy - dy));
        g2.drawLine((int) Math.round(cx + r), (int) Math.round(cy + dy), (int) Math.round(cx - r * 0.45), (int) Math.round(cy + dy));
        flecha(g2, cx + r * 0.9, cy - dy, r * 0.5, 1);
        flecha(g2, cx - r * 0.9, cy + dy, r * 0.5, -1);
    }

    private void flecha(Graphics2D g2, double px, double py, double t, int sentido) {
        Path2D punta = new Path2D.Double();
        punta.moveTo(px, py);
        punta.lineTo(px - sentido * t, py - t * 0.8);
        punta.lineTo(px - sentido * t, py + t * 0.8);
        punta.closePath();
        g2.fill(punta);
    }

    private void texto(Graphics2D g2, String texto, double cx, double cy, Font fuente, Color color) {
        g2.setFont(fuente);
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(color);
        g2.drawString(texto, (float) (cx - fm.stringWidth(texto) / 2.0),
                (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }
}

package lspu_hnn_ui;


import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Graph;

/** Live adjacency list drawn as linked nodes: campus -> [neighbor | km] -> [neighbor | km] -> null. */
public class AdjListPanel extends JPanel {

    private final AppModel model;
    private final boolean compact;

    public AdjListPanel(AppModel model, boolean compact) {
        this.model = model;
        this.compact = compact;
        setOpaque(false);
        model.addListener(() -> {
            revalidate();
            repaint();
        });
    }

    private int rowH() {
        return compact ? 30 : 44;
    }

    private Font font() {
        return Theme.font(Font.BOLD, compact ? 11.5f : 13f);
    }

    private String name(String n) {
        return compact ? Graph.abbr(n) : n;
    }

    private String cellText(Graph.Edge e) {
        return name(e.to()) + " · " + e.km();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(font());
        int widest = 0;
        for (Map.Entry<String, List<Graph.Edge>> en : model.graph.adjacencyList().entrySet()) {
            int w = boxW(fm, name(en.getKey())) + 14;
            for (Graph.Edge e : en.getValue()) w += boxW(fm, cellText(e)) + 30;
            w += 56;
            widest = Math.max(widest, w);
        }
        return new Dimension(Math.max(200, widest), model.graph.nodes().size() * rowH() + 4);
    }

    private static int boxW(FontMetrics fm, String s) {
        return fm.stringWidth(s) + 20;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Ui.aa(g0);
        g.setFont(font());
        FontMetrics fm = g.getFontMetrics();
        int bh = compact ? 22 : 30;
        int y = 0;
        for (Map.Entry<String, List<Graph.Edge>> en : model.graph.adjacencyList().entrySet()) {
            int cy = y + rowH() / 2;
            int x = 0;
            // head box: the campus
            String head = name(en.getKey());
            int w = boxW(fm, head);
            g.setColor(Theme.ACCENT_DARK);
            g.fillRoundRect(x, cy - bh / 2, w, bh, 10, 10);
            g.setColor(Theme.TEXT);
            g.drawString(head, x + 10, cy + (fm.getAscent() - fm.getDescent()) / 2);
            x += w;
            for (Graph.Edge e : en.getValue()) {
                x = arrow(g, x, cy);
                String t = cellText(e);
                w = boxW(fm, t);
                g.setColor(Theme.PANEL_ALT);
                g.fillRoundRect(x, cy - bh / 2, w, bh, 10, 10);
                g.setColor(Theme.BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(x, cy - bh / 2, w, bh, 10, 10);
                g.setColor(Theme.TEXT);
                g.drawString(t, x + 10, cy + (fm.getAscent() - fm.getDescent()) / 2);
                x += w;
            }
            x = arrow(g, x, cy);
            g.setColor(Theme.MUTED);
            g.drawString("null", x + 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
            y += rowH();
        }
        g.dispose();
    }

    /** Short pointer arrow from x; returns the x where the next box starts. */
    private int arrow(Graphics2D g, int x, int cy) {
        g.setColor(Theme.MUTED);
        g.setStroke(new BasicStroke(1.6f));
        g.drawLine(x + 4, cy, x + 22, cy);
        g.fillPolygon(new int[]{x + 26, x + 19, x + 19}, new int[]{cy, cy - 4, cy + 4}, 3);
        return x + 30;
    }
}


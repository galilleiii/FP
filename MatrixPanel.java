package lspu_hnn_ui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Graph;

/** Live adjacency matrix. Rows are "from", columns are "to". INF means no direct connection. */
public class MatrixPanel extends JPanel {

    private final AppModel model;
    private final boolean compact;

    public MatrixPanel(AppModel model, boolean compact) {
        this.model = model;
        this.compact = compact;
        setOpaque(false);
        model.addListener(() -> {
            revalidate();
            repaint();
        });
    }

    private int cw() {
        return compact ? 46 : 96;
    }

    private int ch() {
        return compact ? 25 : 38;
    }

    private int headW() {
        return compact ? 46 : 110;
    }

    @Override
    public Dimension getPreferredSize() {
        int n = model.graph.nodes().size();
        return new Dimension(headW() + n * cw() + 2, (n + 1) * ch() + 2);
    }

    private String name(String n) {
        return compact ? Graph.abbr(n) : n;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Ui.aa(g0);
        List<String> nodes = model.graph.nodes();
        int[][] m = model.graph.matrix();
        int n = nodes.size();
        int cw = cw(), ch = ch(), hw = headW();
        Font head = Theme.font(Font.BOLD, compact ? 11.5f : 12.5f);
        Font cell = Theme.font(Font.BOLD, compact ? 11.5f : 13f);

        for (int j = 0; j < n; j++) center(g, name(nodes.get(j)), head, Theme.MUTED, hw + j * cw, 0, cw, ch);
        for (int i = 0; i < n; i++) {
            int y = (i + 1) * ch;
            center(g, name(nodes.get(i)), head, Theme.MUTED, 0, y, hw, ch);
            for (int j = 0; j < n; j++) {
                int v = m[i][j];
                int x = hw + j * cw;
                boolean link = v != Graph.INF && v != 0;
                g.setColor(link ? new Color(0x1E3A66) : Theme.PANEL_ALT);
                g.fillRoundRect(x + 2, y + 2, cw - 4, ch - 4, 8, 8);
                String s = v == Graph.INF ? "INF" : String.valueOf(v);
                center(g, s, cell, link ? Theme.TEXT : Theme.MUTED, x, y, cw, ch);
            }
        }
        g.dispose();
    }

    private static void center(Graphics2D g, String s, Font f, Color c, int x, int y, int w, int h) {
        g.setFont(f);
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }
}


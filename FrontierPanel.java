package lspu_hnn_ui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

import lspu_hnn_core.Graph;

/** Shows the BFS queue or the DFS stack as small chips, updated at every animation step. */
public class FrontierPanel extends JPanel {

    private final boolean compact;
    private String title = "Queue (front → back)";
    private List<String> items = List.of();
    private boolean stack;

    public FrontierPanel(boolean compact) {
        this.compact = compact;
        setOpaque(false);
    }

    public void set(boolean isStack, List<String> list) {
        stack = isStack;
        title = isStack ? "Stack (top first)" : "Queue (front → back)";
        items = List.copyOf(list);
        repaint();
    }

    public void clear() {
        title = "Queue / stack";
        items = List.of();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(compact ? 200 : 420, compact ? 30 : 38);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Ui.aa(g0);
        g.setFont(Theme.font(Font.BOLD, compact ? 11.5f : 12.5f));
        FontMetrics fm = g.getFontMetrics();
        int cy = getHeight() / 2;
        int base = cy + (fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Theme.MUTED);
        g.drawString(title + ":", 0, base);
        int x = fm.stringWidth(title + ":") + 12;
        if (items.isEmpty()) {
            g.drawString("empty", x, base);
            g.dispose();
            return;
        }
        int h = compact ? 20 : 26;
        for (int i = 0; i < items.size(); i++) {
            String s = compact ? Graph.abbr(items.get(i)) : items.get(i);
            int w = fm.stringWidth(s) + 18;
            boolean head = i == 0;
            g.setColor(head ? Theme.ORANGE : new Color(0x2A3C5F));
            g.fillRoundRect(x, cy - h / 2, w, h, 10, 10);
            g.setColor(head ? Theme.onColor(Theme.ORANGE) : Theme.TEXT);
            g.drawString(s, x + 9, base);
            x += w + 6;
            if (x > getWidth()) break;
        }
        g.dispose();
    }
}

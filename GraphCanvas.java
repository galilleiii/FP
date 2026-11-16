package lspu_hnn_ui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JPanel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Graph;

/**
 * Draws the campus network: nodes, km labels, arrowheads for directed graphs.
 * Colors for nodes and edges are set from outside while a route is animated.
 * Campuses can be dragged to change the layout.
 */
public class GraphCanvas extends JPanel {

    private final AppModel model;
    private final boolean compact;
    private final Map<String, Color> nodeColors = new HashMap<>();
    private final Map<String, Color> edgeColors = new HashMap<>();
    private String dragging;
    private Consumer<String> onNodeClick;

    /** compact = small node circles with initials only (for panels that sit side by side). */
    public GraphCanvas(AppModel model, boolean compact) {
        this.model = model;
        this.compact = compact;
        setOpaque(false);
        model.addListener(this::repaint);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = nodeAt(e.getPoint());
                if (dragging != null && onNodeClick != null) onNodeClick.accept(dragging);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging == null || getWidth() < 10 || getHeight() < 10) return;
                double nx = Math.max(0.06, Math.min(0.94, (e.getX() - marginX()) / (double) (getWidth() - 2 * marginX())));
                double ny = Math.max(0.08, Math.min(0.88, (e.getY() - marginTop()) / (double) (getHeight() - marginTop() - marginBottom())));
                model.graph.setPos(dragging, nx, ny);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = null;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(nodeAt(e.getPoint()) != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void onNodeClick(Consumer<String> c) {
        onNodeClick = c;
    }

    // ---------------------------------------------------------------- state set from outside

    public void clearMarks() {
        nodeColors.clear();
        edgeColors.clear();
        repaint();
    }

    public void markNode(String name, Color c) {
        nodeColors.put(name, c);
        repaint();
    }

    public void markEdge(String a, String b, Color c) {
        edgeColors.put(edgeKey(a, b), c);
        repaint();
    }

    /** Colors every node on the path and every edge between consecutive nodes. */
    public void markPath(List<String> path, Color c) {
        for (int i = 0; i < path.size(); i++) {
            nodeColors.put(path.get(i), c);
            if (i + 1 < path.size()) edgeColors.put(edgeKey(path.get(i), path.get(i + 1)), c);
        }
        repaint();
    }

    private String edgeKey(String a, String b) {
        if (model.graph.isDirected()) return a + "→" + b;
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    @Override
    public Dimension getPreferredSize() {
        return compact ? new Dimension(240, 170) : new Dimension(420, 260);
    }

    // ---------------------------------------------------------------- geometry

    private int radius() {
        return compact ? 15 : 25;
    }

    private int marginX() {
        return compact ? 34 : 60;
    }

    private int marginTop() {
        return compact ? 24 : 34;
    }

    private int marginBottom() {
        return compact ? 24 : 46;   // room for the campus name under the circle
    }

    private Point2D.Double at(String n) {
        double x = marginX() + model.graph.x(n) * (getWidth() - 2.0 * marginX());
        double y = marginTop() + model.graph.y(n) * (getHeight() - marginTop() - marginBottom());
        return new Point2D.Double(x, y);
    }

    private String nodeAt(Point p) {
        for (String n : model.graph.nodes()) {
            if (at(n).distance(p) <= radius() + 4) return n;
        }
        return null;
    }

    // ---------------------------------------------------------------- paint

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Ui.aa(g0);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Graph gr = model.graph;
        boolean directed = gr.isDirected();
        int r = radius();

        Set<String> labelled = new HashSet<>();
        for (Graph.Edge e : gr.edges()) {
            Point2D.Double a = at(e.from()), b = at(e.to());
            double dx = b.x - a.x, dy = b.y - a.y;
            double len = Math.max(1, Math.hypot(dx, dy));
            double ux = dx / len, uy = dy / len, px = -uy, py = ux;
            boolean reverse = directed && gr.weight(e.to(), e.from()) != Graph.INF;
            double off = reverse ? 7 : 0;
            double x1 = a.x + ux * r + px * off, y1 = a.y + uy * r + py * off;
            double x2 = b.x - ux * (r + (directed ? 3 : 0)) + px * off, y2 = b.y - uy * (r + (directed ? 3 : 0)) + py * off;

            Color hl = edgeColors.get(edgeKey(e.from(), e.to()));
            g.setStroke(new BasicStroke(hl != null ? 4f : 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(hl != null ? hl : Theme.EDGE);
            g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            if (directed) arrow(g, x2, y2, ux, uy);

            // distance label near the middle (pushed a little to the side when both directions exist)
            String txt = compact ? String.valueOf(e.km()) : e.km() + " km";
            g.setFont(Theme.font(Font.BOLD, compact ? 10f : 11.5f));
            FontMetrics fm = g.getFontMetrics();
            double t = reverse ? 0.42 : 0.5;
            double lx = x1 + (x2 - x1) * t, ly = y1 + (y2 - y1) * t;
            int w = fm.stringWidth(txt) + 10, h = fm.getHeight() + 2;
            g.setColor(Theme.PANEL);
            g.fillRoundRect((int) lx - w / 2, (int) ly - h / 2, w, h, 8, 8);
            g.setColor(hl != null ? hl : Theme.MUTED);
            g.drawString(txt, (int) lx - fm.stringWidth(txt) / 2, (int) ly + (fm.getAscent() - fm.getDescent()) / 2);
            labelled.add(e.from() + e.to());
        }

        for (String n : gr.nodes()) {
            Point2D.Double p = at(n);
            Color fill = nodeColors.getOrDefault(n, Theme.ACCENT_DARK);
            g.setColor(fill);
            g.fillOval((int) p.x - r, (int) p.y - r, 2 * r, 2 * r);
            g.setStroke(new BasicStroke(2f));
            g.setColor(nodeColors.containsKey(n) ? fill.brighter() : Theme.ACCENT);
            g.drawOval((int) p.x - r, (int) p.y - r, 2 * r, 2 * r);

            String ab = Graph.abbr(n);
            g.setFont(Theme.font(Font.BOLD, compact ? 10f : 13f));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Theme.onColor(fill));
            g.drawString(ab, (int) p.x - fm.stringWidth(ab) / 2, (int) p.y + (fm.getAscent() - fm.getDescent()) / 2);

            if (!compact) {
                g.setFont(Theme.font(Font.PLAIN, 12f));
                fm = g.getFontMetrics();
                g.setColor(Theme.TEXT);
                g.drawString(n, (int) p.x - fm.stringWidth(n) / 2, (int) p.y + r + 16);
            }
        }
        g.dispose();
    }

    private static void arrow(Graphics2D g, double x, double y, double ux, double uy) {
        double size = 10;
        double bx = x - ux * size, by = y - uy * size, px = -uy * size * 0.5, py = ux * size * 0.5;
        g.fillPolygon(new int[]{(int) x, (int) (bx + px), (int) (bx - px)}, new int[]{(int) y, (int) (by + py), (int) (by - py)}, 3);
    }
}

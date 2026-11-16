package lspu_hnn_ui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.BST;

/**
 * Draws the heritage BST. Nodes are placed by in-order position (x) and depth (y), so they never overlap.
 * Node colors are set from outside (marks) while an operation is animated.
 */
public class TreePanel extends JPanel {

    private static final int MX = 34, DX = 56, DY = 76;

    private int my() {
        return fit ? 24 : 36;
    }

    private final AppModel model;
    private final boolean fit;
    private final Map<Integer, Color> marks = new HashMap<>();
    private Integer selected;
    private IntConsumer onNodeClick;

    /** fit = true scales the tree into the panel (dashboard); false keeps a fixed spacing and scrolls. */
    public TreePanel(AppModel model, boolean fit) {
        this.model = model;
        this.fit = fit;
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
        model.addListener(() -> {
            revalidate();
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Integer k = nodeAt(e.getPoint());
                if (k != null && onNodeClick != null) onNodeClick.accept(k);
            }
        });
    }

    public void onNodeClick(IntConsumer c) {
        onNodeClick = c;
    }

    public void mark(int key, Color c) {
        marks.put(key, c);
        repaint();
    }

    public void clearMarks() {
        marks.clear();
        repaint();
    }

    public void setSelected(Integer key) {
        selected = key;
        repaint();
    }

    public Integer selected() {
        return selected;
    }

    @Override
    public Dimension getPreferredSize() {
        if (fit) return new Dimension(320, 220);
        int n = model.bst.size(), levels = model.bst.height();
        return new Dimension(Math.max(520, 2 * MX + Math.max(0, n - 1) * DX), Math.max(320, 2 * my() + Math.max(0, levels - 1) * DY + 20));
    }

    // ---------------------------------------------------------------- layout

    private Map<Integer, Point> positions() {
        Map<Integer, Point> pts = new HashMap<>();
        int n = model.bst.size();
        if (n == 0) return pts;
        int levels = model.bst.height();
        double sx, sy, x0;
        if (fit) {
            sx = n > 1 ? (getWidth() - 2.0 * MX) / (n - 1) : 0;
            sy = levels > 1 ? Math.min(72, (getHeight() - 2.0 * my()) / (levels - 1)) : 0;
            x0 = n > 1 ? MX : getWidth() / 2.0;
        } else {
            sx = n > 1 ? Math.max(DX, Math.min(96, (getWidth() - 2.0 * MX) / (n - 1))) : DX;
            sy = levels > 1 ? Math.max(DY, Math.min(104, (getHeight() - 2.0 * my()) / (levels - 1))) : DY;
            x0 = Math.max(MX, (getWidth() - (n - 1) * sx) / 2.0);
        }
        place(model.bst.root(), 0, new int[]{0}, x0, sx, sy, pts);
        return pts;
    }

    private void place(BST.Node nd, int depth, int[] idx, double x0, double sx, double sy, Map<Integer, Point> pts) {
        if (nd == null) return;
        place(nd.left, depth + 1, idx, x0, sx, sy, pts);
        pts.put(nd.key(), new Point((int) Math.round(x0 + idx[0] * sx), (int) Math.round(my() + depth * sy)));
        idx[0]++;
        place(nd.right, depth + 1, idx, x0, sx, sy, pts);
    }

    private int radius() {
        if (!fit) return 20;
        int n = Math.max(1, model.bst.size());
        double sx = n > 1 ? (getWidth() - 2.0 * MX) / (n - 1) : 40;
        return (int) Math.max(10, Math.min(17, sx * 0.45));
    }

    private Integer nodeAt(Point p) {
        int r = radius();
        for (Map.Entry<Integer, Point> e : positions().entrySet()) {
            if (e.getValue().distance(p) <= r) return e.getKey();
        }
        return null;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        Integer k = nodeAt(e.getPoint());
        if (k == null) return null;
        BST.Node n = model.bst.find(k);
        return n == null ? null : "<html><b>" + k + "</b><br>" + n.event.title() + "</html>";
    }

    // ---------------------------------------------------------------- paint

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Ui.aa(g0);
        Map<Integer, Point> pts = positions();
        if (pts.isEmpty()) {
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(Font.PLAIN, 14f));
            String s = "No events yet";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, getHeight() / 2);
            g.dispose();
            return;
        }
        g.setStroke(new BasicStroke(2f));
        g.setColor(Theme.EDGE);
        edges(g, model.bst.root(), pts);

        int r = radius();
        g.setFont(Theme.font(Font.BOLD, r >= 17 ? 12f : 9.5f));
        FontMetrics fm = g.getFontMetrics();
        for (Map.Entry<Integer, Point> e : pts.entrySet()) {
            int key = e.getKey();
            Point p = e.getValue();
            Color fill = marks.getOrDefault(key, Theme.NODE);
            g.setColor(fill);
            g.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
            boolean sel = selected != null && selected == key;
            g.setStroke(new BasicStroke(sel ? 3f : 2f));
            g.setColor(sel ? Color.WHITE : (marks.containsKey(key) ? fill.brighter() : Theme.ACCENT));
            g.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);
            g.setColor(Theme.onColor(fill));
            String s = String.valueOf(key);
            g.drawString(s, p.x - fm.stringWidth(s) / 2, p.y + (fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
    }

    private void edges(Graphics2D g, BST.Node n, Map<Integer, Point> pts) {
        if (n == null) return;
        Point p = pts.get(n.key());
        if (n.left != null) {
            Point c = pts.get(n.left.key());
            g.drawLine(p.x, p.y, c.x, c.y);
            edges(g, n.left, pts);
        }
        if (n.right != null) {
            Point c = pts.get(n.right.key());
            g.drawLine(p.x, p.y, c.x, c.y);
            edges(g, n.right, pts);
        }
    }
}

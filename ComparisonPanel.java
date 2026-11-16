package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Graph;
import lspu_hnn_core.GraphSearch;

/** BFS and DFS side by side: path, hops, km, nodes visited, peak queue/stack, run time, complexity. */
public class ComparisonPanel extends JPanel {

    private final AppModel model;
    private final boolean compact;
    private final DefaultTableModel table;
    private final JLabel note = Ui.muted(" ");
    private GraphSearch.Result bfs, dfs;

    public ComparisonPanel(AppModel model, boolean compact) {
        super(new BorderLayout(0, 8));
        this.model = model;
        this.compact = compact;
        setOpaque(false);
        table = new DefaultTableModel(new String[]{"", "BFS", "DFS"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(table);
        Ui.styleTable(t);
        t.setRowHeight(compact ? 24 : 28);
        t.getColumnModel().getColumn(0).setPreferredWidth(compact ? 150 : 170);
        t.getColumnModel().getColumn(1).setPreferredWidth(compact ? 150 : 300);
        t.getColumnModel().getColumn(2).setPreferredWidth(compact ? 150 : 300);
        add(Ui.scroll(t), BorderLayout.CENTER);
        note.setFont(Theme.font(Font.PLAIN, 12f));
        add(note, BorderLayout.SOUTH);
        model.addListener(this::clear);
        rebuild();
    }

    public void clear() {
        bfs = null;
        dfs = null;
        rebuild();
    }

    public void set(GraphSearch.Result r) {
        if ("BFS".equals(r.algorithm)) bfs = r;
        else dfs = r;
        rebuild();
    }

    private String path(GraphSearch.Result r) {
        if (r == null) return "–";
        if (!r.found) return "No route";
        List<String> p = new ArrayList<>();
        for (String n : r.path) p.add(compact ? Graph.abbr(n) : n);
        return String.join(" → ", p);
    }

    private static String visited(GraphSearch.Result r, boolean compact) {
        if (r == null) return "–";
        List<String> p = new ArrayList<>();
        for (String n : r.visitOrder) p.add(compact ? Graph.abbr(n) : n);
        return String.join(", ", p);
    }

    private interface Get {
        String of(GraphSearch.Result r);
    }

    private void row(String label, Get g) {
        table.addRow(new Object[]{label, bfs == null ? "–" : g.of(bfs), dfs == null ? "–" : g.of(dfs)});
    }

    private void rebuild() {
        table.setRowCount(0);
        table.addRow(new Object[]{"Path found", path(bfs), path(dfs)});
        row("Hops", r -> r.found ? String.valueOf(r.hops) : "–");
        row("Total distance", r -> r.found ? r.km + " km" : "–");
        row("Nodes visited", r -> String.valueOf(r.visitOrder.size()));
        if (!compact) table.addRow(new Object[]{"Visit order", visited(bfs, false), visited(dfs, false)});
        row("Peak queue / stack", r -> String.valueOf(r.maxFrontier));
        row("Run time (avg)", r -> String.format(Locale.ROOT, "%.1f µs", r.avgMicros));
        row("Time / space", r -> r.time + " / " + r.space);
        row("Guarantee", r -> r.guarantee);
        note.setText(buildNote());
    }

    private String buildNote() {
        if (bfs == null || dfs == null) return "Run both BFS and DFS between the same two campuses to compare them.";
        if (!bfs.found || !dfs.found) return "At least one search found no route, so there is nothing to compare.";
        if (!bfs.path.get(0).equals(dfs.path.get(0)) || !bfs.path.get(bfs.path.size() - 1).equals(dfs.path.get(dfs.path.size() - 1))) {
            return "BFS and DFS used different start or end campuses. Run both on the same pair.";
        }
        GraphSearch.Result d = GraphSearch.dijkstra(model.graph, bfs.path.get(0), bfs.path.get(bfs.path.size() - 1));
        String who = bfs.hops < dfs.hops ? "BFS found a route with fewer hops."
                : bfs.hops == dfs.hops ? "Both routes have the same number of hops." : "DFS happened to find the shorter route here.";
        return who + " Shortest by distance (Dijkstra, for reference): " + d.km + " km.";
    }
}


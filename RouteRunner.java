package lspu_hnn_ui;


import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.GraphSearch;

/**
 * Plays a BFS or DFS run on a GraphCanvas, step by step. Used by the route page and the dashboard.
 * Colors: dim yellow = already visited, yellow = current, orange = waiting in the queue/stack, green = final path.
 */
public final class RouteRunner {

    private final AppModel model;
    private final GraphCanvas canvas;
    private final FrontierPanel frontier;
    private final Consumer<String> out;
    private final StepPlayer<GraphSearch.Trace> player;

    public RouteRunner(AppModel model, GraphCanvas canvas, FrontierPanel frontier, Consumer<String> out, IntSupplier delayMs) {
        this.model = model;
        this.canvas = canvas;
        this.frontier = frontier;
        this.out = out;
        this.player = new StepPlayer<>(delayMs);
    }

    public boolean busy() {
        return player.running();
    }

    public void reset() {
        player.stop();
        canvas.clearMarks();
        frontier.clear();
    }

    public void run(boolean bfs, String start, String end, Consumer<GraphSearch.Result> onDone) {
        if (player.running()) {
            out.accept("Wait for the current run to finish (or press Reset).");
            return;
        }
        if (start == null || end == null) {
            out.accept("Pick a start and an end campus.");
            return;
        }
        GraphSearch.Result r;
        try {
            r = bfs ? GraphSearch.bfs(model.graph, start, end) : GraphSearch.dfs(model.graph, start, end);
        } catch (IllegalArgumentException ex) {
            out.accept(ex.getMessage());
            return;
        }
        canvas.clearMarks();
        out.accept(r.algorithm + ": " + start + " → " + end + (model.graph.isDirected() ? " (directed)" : ""));
        player.play(r.traces, t -> {
            canvas.clearMarks();
            for (String v : t.visited()) canvas.markNode(v, Theme.PASSED);
            for (String f : t.frontier()) canvas.markNode(f, Theme.ORANGE);
            if (t.current() != null) canvas.markNode(t.current(), Theme.YELLOW);
            frontier.set(!bfs, t.frontier());
            out.accept("  " + t.note());
        }, () -> {
            canvas.clearMarks();
            for (String v : r.visitOrder) canvas.markNode(v, Theme.PASSED);
            frontier.clear();
            if (r.found) {
                canvas.markPath(r.path, Theme.GREEN);
                out.accept("Path: " + String.join(" → ", r.path) + "  (" + r.hops + " hops, " + r.km + " km)");
            } else {
                out.accept("No route from " + start + " to " + end + (model.graph.isDirected() ? " (check the edge directions)." : "."));
            }
            if (onDone != null) onDone.accept(r);
        });
    }

    /** Copy of the visit order for tests. */
    public static List<String> orderOf(GraphSearch.Result r) {
        return r.visitOrder;
    }
}

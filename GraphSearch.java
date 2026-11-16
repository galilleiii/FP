package lspu_hnn_core;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BFS and DFS between two campuses. Each run records a trace (one entry per animation step)
 * so the GUI can show the active node and the queue / stack at every moment.
 * Dijkstra is included only as a reference, because BFS and DFS do not minimize kilometers.
 */
public final class GraphSearch {

    private GraphSearch() {
    }

    /** One animation step: the active node plus the queue (BFS) or stack (DFS) after that step. */
    public record Trace(String current, List<String> frontier, List<String> visited, String note) {
    }

    public static final class Result {
        public String algorithm;
        public boolean found;
        public final List<Trace> traces = new ArrayList<>();
        public List<String> visitOrder = new ArrayList<>();
        public List<String> path = new ArrayList<>();
        public int hops;
        public int km;
        public int maxFrontier;
        public double avgMicros;
        public String time, space, guarantee;
    }

    private static void check(Graph g, String s, String e) {
        if (!g.hasNode(s) || !g.hasNode(e)) throw new IllegalArgumentException("Pick a start and an end campus");
    }

    // ---------------------------------------------------------------- BFS

    public static Result bfs(Graph g, String start, String end) {
        check(g, start, end);
        Result r = new Result();
        r.algorithm = "BFS";
        runBfs(g, start, end, r);
        r.avgMicros = timeIt(() -> runBfs(g, start, end, null));
        r.time = "O(V + E)";
        r.space = "O(V)";
        r.guarantee = "Fewest hops";
        return r;
    }

    private static void runBfs(Graph g, String s, String e, Result r) {
        Deque<String> queue = new ArrayDeque<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> seen = new HashSet<>();
        List<String> order = new ArrayList<>();
        queue.add(s);
        seen.add(s);
        parent.put(s, null);
        int maxF = 1;
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            order.add(cur);
            if (cur.equals(e)) {
                if (r != null) r.traces.add(new Trace(cur, new ArrayList<>(queue), new ArrayList<>(order),
                        "Dequeue " + cur + ": destination reached"));
                break;
            }
            List<String> added = new ArrayList<>();
            for (Graph.Edge ed : g.neighbors(cur)) {
                if (seen.add(ed.to())) {
                    parent.put(ed.to(), cur);
                    queue.add(ed.to());
                    added.add(ed.to());
                }
            }
            maxF = Math.max(maxF, queue.size());
            if (r != null) r.traces.add(new Trace(cur, new ArrayList<>(queue), new ArrayList<>(order),
                    "Dequeue " + cur + (added.isEmpty() ? ": nothing new to add" : ": enqueue " + String.join(", ", added))));
        }
        finish(g, r, parent, e, order, maxF);
    }

    // ---------------------------------------------------------------- DFS

    public static Result dfs(Graph g, String start, String end) {
        check(g, start, end);
        Result r = new Result();
        r.algorithm = "DFS";
        runDfs(g, start, end, r);
        r.avgMicros = timeIt(() -> runDfs(g, start, end, null));
        r.time = "O(V + E)";
        r.space = "O(V)";
        r.guarantee = "None (first path found)";
        return r;
    }

    /** Depth-first search with an explicit stack that holds the current branch (so it never exceeds V nodes). */
    private static void runDfs(Graph g, String s, String e, Result r) {
        Deque<String> stack = new ArrayDeque<>();
        Map<String, Integer> next = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();
        stack.push(s);
        visited.add(s);
        parent.put(s, null);
        order.add(s);
        int maxF = 1;
        if (r != null) r.traces.add(new Trace(s, stackList(stack), new ArrayList<>(order), "Start at " + s));
        while (!stack.isEmpty() && !stack.peek().equals(e)) {
            String cur = stack.peek();
            List<Graph.Edge> nb = g.neighbors(cur);
            int i = next.getOrDefault(cur, 0);
            while (i < nb.size() && visited.contains(nb.get(i).to())) i++;
            if (i < nb.size()) {
                next.put(cur, i + 1);
                String nx = nb.get(i).to();
                visited.add(nx);
                parent.put(nx, cur);
                order.add(nx);
                stack.push(nx);
                maxF = Math.max(maxF, stack.size());
                if (r != null) r.traces.add(new Trace(nx, stackList(stack), new ArrayList<>(order),
                        "Go deeper from " + cur + " to " + nx + (nx.equals(e) ? ": destination reached" : "")));
            } else {
                stack.pop();
                if (r != null) r.traces.add(new Trace(stack.isEmpty() ? null : stack.peek(), stackList(stack),
                        new ArrayList<>(order), "Dead end at " + cur + ": backtrack"));
            }
        }
        finish(g, r, parent, e, order, maxF);
    }

    private static List<String> stackList(Deque<String> stack) {
        return new ArrayList<>(stack);   // top first
    }

    // ---------------------------------------------------------------- Dijkstra (reference only)

    public static Result dijkstra(Graph g, String start, String end) {
        check(g, start, end);
        Result r = new Result();
        r.algorithm = "Dijkstra";
        runDijkstra(g, start, end, r);
        r.avgMicros = timeIt(() -> runDijkstra(g, start, end, null));
        r.time = "O(V^2)";
        r.space = "O(V)";
        r.guarantee = "Least total km";
        return r;
    }

    private static void runDijkstra(Graph g, String s, String e, Result r) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> done = new HashSet<>();
        List<String> order = new ArrayList<>();
        for (String n : g.nodes()) dist.put(n, Graph.INF);
        dist.put(s, 0);
        parent.put(s, null);
        while (true) {
            String best = null;
            for (String n : g.nodes()) {
                if (!done.contains(n) && dist.get(n) != Graph.INF && (best == null || dist.get(n) < dist.get(best))) best = n;
            }
            if (best == null) break;
            done.add(best);
            order.add(best);
            if (best.equals(e)) break;
            for (Graph.Edge ed : g.neighbors(best)) {
                int nd = dist.get(best) + ed.km();
                if (nd < dist.get(ed.to())) {
                    dist.put(ed.to(), nd);
                    parent.put(ed.to(), best);
                }
            }
        }
        finish(g, r, parent, e, order, 0);
    }

    // ---------------------------------------------------------------- shared

    private static void finish(Graph g, Result r, Map<String, String> parent, String end, List<String> order, int maxF) {
        if (r == null) return;
        r.visitOrder = order;
        r.maxFrontier = maxF;
        r.found = order.contains(end);
        if (r.found) {
            List<String> p = new ArrayList<>();
            for (String n = end; n != null; n = parent.get(n)) p.add(n);
            Collections.reverse(p);
            r.path = p;
            r.hops = p.size() - 1;
            int km = 0;
            for (int i = 0; i + 1 < p.size(); i++) km += g.weight(p.get(i), p.get(i + 1));
            r.km = km;
        }
    }

    private static double timeIt(Runnable job) {
        for (int i = 0; i < 300; i++) job.run();      // warm-up
        int n = 1000;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) job.run();
        return (System.nanoTime() - t0) / 1000.0 / n;  // microseconds per run
    }
}


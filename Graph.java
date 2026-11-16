package lspu_hnn_core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Campus network. One edge list is the single source of truth: the adjacency matrix
 * and the adjacency list are both generated from it, so they can never disagree.
 * Undirected edges are stored once and work both ways.
 */
public class Graph {

    /** "No connection" in the matrix. */
    public static final int INF = Integer.MAX_VALUE;

    public record Edge(String from, String to, int km) {
    }

    private final List<String> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<String, double[]> pos = new HashMap<>();   // 0..1 layout for the canvas
    private boolean directed;

    // ---------------------------------------------------------------- basics

    public List<String> nodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public boolean isDirected() {
        return directed;
    }

    /** Switching to undirected merges A-to-B and B-to-A pairs into one edge. */
    public void setDirected(boolean d) {
        directed = d;
        if (!d) {
            List<Edge> keep = new ArrayList<>();
            for (Edge e : edges) {
                boolean dup = false;
                for (Edge k : keep) {
                    if (k.from().equals(e.to()) && k.to().equals(e.from())) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) keep.add(e);
            }
            edges.clear();
            edges.addAll(keep);
        }
    }

    public void clear() {
        nodes.clear();
        edges.clear();
        pos.clear();
        directed = false;
    }

    public boolean hasNode(String name) {
        return nodes.contains(name);
    }

    /** Short label for tight spaces: initials of each word, or the first 3 letters of a single word. */
    public static String abbr(String name) {
        String[] w = name.trim().split("\\s+");
        if (w.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (String s : w) if (!s.isEmpty()) sb.append(Character.toUpperCase(s.charAt(0)));
            return sb.toString();
        }
        return name.substring(0, Math.min(3, name.length())).toUpperCase();
    }

    // ---------------------------------------------------------------- nodes

    public boolean addNode(String name) {
        return addNode(name, Double.NaN, Double.NaN);
    }

    /** Returns false if a campus with that name already exists. */
    public boolean addNode(String name, double x, double y) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Campus name cannot be empty");
        if (name.length() > 24) throw new IllegalArgumentException("Campus name is too long (24 characters max)");
        for (String n : nodes) if (n.equalsIgnoreCase(name)) return false;
        if (Double.isNaN(x) || Double.isNaN(y)) {
            double[] p = freeSpot();
            x = p[0];
            y = p[1];
        }
        nodes.add(name);
        pos.put(name, new double[]{x, y});
        return true;
    }

    /** Picks the point farthest from every existing campus and every drawn line, so a new campus does not land on top of anything. */
    private double[] freeSpot() {
        double bestScore = -1, bx = 0.5, by = 0.5;
        for (int i = 0; i <= 14; i++) {
            for (int j = 0; j <= 9; j++) {
                double cx = 0.08 + 0.84 * i / 14.0, cy = 0.12 + 0.74 * j / 9.0;
                double score = Double.MAX_VALUE;
                for (String n : nodes) score = Math.min(score, Math.hypot(cx - x(n), (cy - y(n)) * 0.8));
                for (Edge e : edges) {
                    score = Math.min(score, 1.6 * segmentDistance(cx, cy, x(e.from()), y(e.from()), x(e.to()), y(e.to())));
                }
                if (score > bestScore) {
                    bestScore = score;
                    bx = cx;
                    by = cy;
                }
            }
        }
        return new double[]{bx, by};
    }

    private static double segmentDistance(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len2));
        return Math.hypot(px - (ax + t * dx), (py - (ay + t * dy)) * 0.8);
    }

    public boolean removeNode(String name) {
        if (!nodes.remove(name)) return false;
        edges.removeIf(e -> e.from().equals(name) || e.to().equals(name));
        pos.remove(name);
        return true;
    }

    public double x(String name) {
        double[] p = pos.get(name);
        return p == null ? 0.5 : p[0];
    }

    public double y(String name) {
        double[] p = pos.get(name);
        return p == null ? 0.5 : p[1];
    }

    public void setPos(String name, double x, double y) {
        if (nodes.contains(name)) pos.put(name, new double[]{x, y});
    }

    // ---------------------------------------------------------------- edges

    private int indexOfEdge(String a, String b) {
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (e.from().equals(a) && e.to().equals(b)) return i;
            if (!directed && e.from().equals(b) && e.to().equals(a)) return i;
        }
        return -1;
    }

    /** Adds the connection, or changes its distance if it already exists. */
    public void setEdge(String a, String b, int km) {
        if (!nodes.contains(a) || !nodes.contains(b)) throw new IllegalArgumentException("Pick two existing campuses");
        if (a.equals(b)) throw new IllegalArgumentException("A campus cannot connect to itself");
        if (km < 1 || km > 9999) throw new IllegalArgumentException("Distance must be between 1 and 9999 km");
        int i = indexOfEdge(a, b);
        if (i >= 0) {
            Edge old = edges.get(i);
            edges.set(i, new Edge(old.from(), old.to(), km));
        } else {
            edges.add(new Edge(a, b, km));
        }
    }

    public boolean removeEdge(String a, String b) {
        int i = indexOfEdge(a, b);
        if (i < 0) return false;
        edges.remove(i);
        return true;
    }

    /** Distance from a to b, or INF if there is no direct connection. */
    public int weight(String a, String b) {
        int i = indexOfEdge(a, b);
        return i < 0 ? INF : edges.get(i).km();
    }

    // ---------------------------------------------------------------- the two representations

    /** Adjacency matrix in node order. Diagonal is 0, no connection is INF. */
    public int[][] matrix() {
        int n = nodes.size();
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m[i][j] = i == j ? 0 : weight(nodes.get(i), nodes.get(j));
            }
        }
        return m;
    }

    /** Neighbors of a campus (edge.from is the campus), ordered like the node list so searches are repeatable. */
    public List<Edge> neighbors(String node) {
        List<Edge> out = new ArrayList<>();
        for (Edge e : edges) {
            if (e.from().equals(node)) out.add(new Edge(node, e.to(), e.km()));
            else if (!directed && e.to().equals(node)) out.add(new Edge(node, e.from(), e.km()));
        }
        out.sort((p, q) -> Integer.compare(nodes.indexOf(p.to()), nodes.indexOf(q.to())));
        return out;
    }

    public Map<String, List<Edge>> adjacencyList() {
        Map<String, List<Edge>> m = new LinkedHashMap<>();
        for (String n : nodes) m.put(n, neighbors(n));
        return m;
    }
}

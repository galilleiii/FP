package lspu_hnn_core;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Plain tab-separated text files in the data folder (UTF-8, so "Los Baños" is kept):
 * events.txt, campuses.txt, users.txt, activity_log.txt.
 * The tree is saved in pre-order so it loads back with exactly the same shape.
 */
final class Storage {

    private final Path dir;

    Storage(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create data folder " + dir, e);
        }
    }

    private Path file(String name) {
        return dir.resolve(name);
    }

    private static String clean(String s) {
        return s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    private void write(String name, List<String> lines) {
        Path target = file(name);
        Path tmp = file(name + ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + name, e);
        }
    }

    private List<String> read(String name) {
        Path p = file(name);
        if (!Files.exists(p)) return null;
        try {
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + name, e);
        }
    }

    // ---------------------------------------------------------------- events

    void saveEvents(BST bst) {
        List<String> lines = new ArrayList<>();
        for (Event e : bst.preorder()) lines.add(e.year() + "\t" + clean(e.title()));
        write("events.txt", lines);
    }

    boolean loadEvents(BST bst) {
        List<String> lines = read("events.txt");
        if (lines == null) return false;
        bst.clear();
        for (String l : lines) {
            String[] p = l.split("\t", 2);
            if (p.length == 2) {
                try {
                    bst.insert(new Event(Integer.parseInt(p[0].trim()), p[1]));
                } catch (NumberFormatException ignored) {
                    // skip a damaged line
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- campuses

    void saveGraph(Graph g) {
        List<String> lines = new ArrayList<>();
        lines.add("DIRECTED\t" + g.isDirected());
        for (String n : g.nodes()) {
            lines.add(String.format(Locale.ROOT, "NODE\t%s\t%.4f\t%.4f", clean(n), g.x(n), g.y(n)));
        }
        for (Graph.Edge e : g.edges()) lines.add("EDGE\t" + clean(e.from()) + "\t" + clean(e.to()) + "\t" + e.km());
        write("campuses.txt", lines);
    }

    boolean loadGraph(Graph g) {
        List<String> lines = read("campuses.txt");
        if (lines == null) return false;
        g.clear();
        boolean directed = false;
        List<String[]> edgeRows = new ArrayList<>();
        for (String l : lines) {
            String[] p = l.split("\t");
            try {
                switch (p[0]) {
                    case "DIRECTED" -> directed = Boolean.parseBoolean(p[1]);
                    case "NODE" -> g.addNode(p[1], Double.parseDouble(p[2]), Double.parseDouble(p[3]));
                    case "EDGE" -> edgeRows.add(p);
                    default -> {
                    }
                }
            } catch (RuntimeException ignored) {
                // skip a damaged line
            }
        }
        g.setDirected(directed);
        for (String[] p : edgeRows) {
            try {
                g.setEdge(p[1], p[2], Integer.parseInt(p[3]));
            } catch (RuntimeException ignored) {
                // skip a damaged line
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- users

    void saveUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(u.username + "\t" + clean(u.displayName) + "\t" + u.salt + "\t" + u.hash + "\t" + u.role + "\t" + u.active);
        }
        write("users.txt", lines);
    }

    List<User> loadUsers() {
        List<String> lines = read("users.txt");
        if (lines == null) return null;
        List<User> out = new ArrayList<>();
        for (String l : lines) {
            String[] p = l.split("\t");
            if (p.length == 6) {
                try {
                    out.add(new User(p[0], p[1], p[2], p[3], Role.valueOf(p[4]), Boolean.parseBoolean(p[5])));
                } catch (IllegalArgumentException ignored) {
                    // skip a damaged line
                }
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- activity log

    void appendLog(ActivityLog.Entry e) {
        String line = e.time() + "\t" + clean(e.user()) + "\t" + clean(e.action()) + "\t" + clean(e.detail()) + "\n";
        try {
            Files.writeString(file("activity_log.txt"), line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not write the activity log", ex);
        }
    }

    List<ActivityLog.Entry> loadLog() {
        List<String> lines = read("activity_log.txt");
        List<ActivityLog.Entry> out = new ArrayList<>();
        if (lines == null) return out;
        for (String l : lines) {
            String[] p = l.split("\t", 4);
            if (p.length == 4) {
                try {
                    out.add(new ActivityLog.Entry(Long.parseLong(p[0]), p[1], p[2], p[3]));
                } catch (NumberFormatException ignored) {
                    // skip a damaged line
                }
            }
        }
        return out;
    }
}


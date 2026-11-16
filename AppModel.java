package lspu_hnn_core;


import java.util.Locale;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Everything the app knows: the history tree, the campus graph, accounts and the activity log.
 * Every method that changes data first checks that an admin is logged in, so the rule is enforced here
 * and not only by hiding buttons in the GUI. Each change is saved to disk, logged, and announced to listeners.
 */
public class AppModel {

    public final BST bst = new BST();
    public final Graph graph = new Graph();
    public final Auth auth = new Auth();
    public final ActivityLog log;

    private final Storage storage;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private User current;
    private final Profiles profiles;

    public AppModel(Path dataDir) {
        storage = new Storage(dataDir);

        profiles = new Profiles(dataDir.resolve("profiles.csv"));
 
        log = new ActivityLog(storage);
        if (!storage.loadEvents(bst)) {
            SampleData.fillEvents(bst);
            storage.saveEvents(bst);
        }
        if (!storage.loadGraph(graph)) {
            SampleData.fillGraph(graph);
            storage.saveGraph(graph);
        }
        List<User> loaded = storage.loadUsers();
        if (loaded == null || loaded.isEmpty()) {
            auth.seed();
            storage.saveUsers(auth.users());
        } else {
            auth.setUsers(loaded);
        }
        
    }

    // ---------------------------------------------------------------- listeners

    public void addListener(Runnable r) {
        listeners.add(r);
    }

    public void fire() {
        for (Runnable r : listeners) r.run();
    }

    // ---------------------------------------------------------------- session

    public User currentUser() {
        return current;
    }

    public boolean isAdmin() {
        return current != null && current.role() == Role.ADMIN;
    }

    public User login(String username, char[] password) throws Auth.AuthException {
        try {
            User u = auth.authenticate(username, password);
            current = u;
            log.add(u.username(), "LOGIN", u.role().label());
            fire();
            return u;
        } catch (Auth.AuthException e) {
            log.add("-", "LOGIN_FAILED", username == null ? "" : username.trim());
            throw e;
        }
    }

    public void logout() {
        if (current != null) log.add(current.username(), "LOGOUT", "");
        current = null;
    }
    

    /** Lets people sign in with a student ID or email: returns the matching account ID. */
    public String resolveLoginId(String typed) {
        String t = typed == null ? "" : typed.trim();
        for (User u : auth.users()) {
            if (u.username().equalsIgnoreCase(t)) return u.username();
        }
        String name = profiles.usernameFor(t);
        return name != null ? name : t;
    }
 
    /** Self sign-up. Always creates a Student account (never an admin). */
    public void register(String fullName, String studentId, String email, String campus,
                         String loginId, char[] password) {
        fullName = fullName == null ? "" : fullName.trim();
        studentId = studentId == null ? "" : studentId.trim();
        email = email == null ? "" : email.trim();
        campus = campus == null ? "" : campus.trim();
        if (fullName.length() < 2) throw new IllegalArgumentException("Enter your full name");
        if (!studentId.matches("[A-Za-z0-9-]{4,20}")) throw new IllegalArgumentException("Enter a valid student ID");
        if (!email.toLowerCase(Locale.ROOT).matches("[a-z0-9._%+-]+@lspu\\.edu\\.ph")) {
            throw new IllegalArgumentException("Use your institutional email (name@lspu.edu.ph)");
        }
        if (campus.isEmpty()) throw new IllegalArgumentException("Choose your host campus");
        if (profiles.studentIdTaken(studentId)) throw new IllegalArgumentException("That student ID is already registered");
        if (profiles.emailTaken(email)) throw new IllegalArgumentException("That email is already registered");
 
        auth.add(loginId, fullName, password, Role.USER);   // checks ID format, duplicates and password length
        storage.saveUsers(auth.users());
        profiles.add(new Profiles.Profile(loginId.trim(), studentId, email, campus));
        log.add("-", "REGISTER", loginId.trim() + " (" + Role.USER.label() + ")");
        fire();
    }

    private void requireAdmin() {
        if (!isAdmin()) throw new SecurityException("Admin access required");
    }

    private void record(String action, String detail) {
        log.add(current == null ? "-" : current.username(), action, detail);
    }

    // ---------------------------------------------------------------- heritage events (admin)

    /** Returns false if the year already exists. */
    public boolean insertEvent(int year, String title) {
        requireAdmin();
        title = title == null ? "" : title.trim();
        if (year < 1 || year > 9999) throw new IllegalArgumentException("Year must be between 1 and 9999");
        if (title.isEmpty()) throw new IllegalArgumentException("Event title cannot be empty");
        if (title.length() > 120) throw new IllegalArgumentException("Event title is too long (120 characters max)");
        if (!bst.insert(new Event(year, title))) return false;
        storage.saveEvents(bst);
        record("INSERT_EVENT", year + " " + title);
        fire();
        return true;
    }

    /** Returns false if the year is not in the tree. */
    public boolean deleteEvent(int year) {
        requireAdmin();
        BST.Node n = bst.find(year);
        String title = n == null ? "" : n.event.title();
        if (!bst.delete(year)) return false;
        storage.saveEvents(bst);
        record("DELETE_EVENT", year + " " + title);
        fire();
        return true;
    }

    // ---------------------------------------------------------------- campus graph (admin)

    public boolean addCampus(String name) {
        requireAdmin();
        if (!graph.addNode(name)) return false;
        storage.saveGraph(graph);
        record("ADD_CAMPUS", name.trim());
        fire();
        return true;
    }

    public boolean removeCampus(String name) {
        requireAdmin();
        if (graph.nodes().size() <= 2) throw new IllegalStateException("Keep at least two campuses");
        if (!graph.removeNode(name)) return false;
        storage.saveGraph(graph);
        record("REMOVE_CAMPUS", name);
        fire();
        return true;
    }

    public void setEdge(String a, String b, int km) {
        requireAdmin();
        graph.setEdge(a, b, km);
        storage.saveGraph(graph);
        record("SET_EDGE", a + (graph.isDirected() ? " -> " : " <-> ") + b + " = " + km + " km");
        fire();
    }

    public boolean removeEdge(String a, String b) {
        requireAdmin();
        if (!graph.removeEdge(a, b)) return false;
        storage.saveGraph(graph);
        record("REMOVE_EDGE", a + " - " + b);
        fire();
        return true;
    }

    public void setDirected(boolean directed) {
        requireAdmin();
        graph.setDirected(directed);
        storage.saveGraph(graph);
        record("SET_DIRECTED", String.valueOf(directed));
        fire();
    }

    // ---------------------------------------------------------------- import / export / reset (admin)

    public void exportEvents(Path file) {
        requireAdmin();
        List<String> lines = new ArrayList<>();
        lines.add("year,title");
        for (Event e : bst.inorder()) lines.add(e.year() + "," + Csv.quote(e.title()));
        writeLines(file, lines);
        record("EXPORT_EVENTS", file.getFileName().toString());
    }

    /** Returns {added, skipped}. Skipped rows are duplicates or invalid lines. */
    public int[] importEvents(Path file) {
        requireAdmin();
        int added = 0, skipped = 0;
        for (String line : readLines(file)) {
            if (line.isBlank()) continue;
            List<String> c = Csv.parse(line);
            try {
                int year = Integer.parseInt(c.get(0));
                String title = c.get(1);
                if (year >= 1 && year <= 9999 && !title.isBlank() && title.length() <= 120
                        && bst.insert(new Event(year, title))) added++;
                else skipped++;
            } catch (RuntimeException ex) {
                if (!line.toLowerCase().startsWith("year")) skipped++;   // header row is not counted
            }
        }
        storage.saveEvents(bst);
        record("IMPORT_EVENTS", added + " added, " + skipped + " skipped");
        fire();
        return new int[]{added, skipped};
    }

    public void exportEdges(Path file) {
        requireAdmin();
        List<String> lines = new ArrayList<>();
        lines.add("from,to,km");
        for (Graph.Edge e : graph.edges()) lines.add(Csv.quote(e.from()) + "," + Csv.quote(e.to()) + "," + e.km());
        writeLines(file, lines);
        record("EXPORT_EDGES", file.getFileName().toString());
    }

    /** Adds missing campuses and sets each distance. Returns the number of connections applied. */
    public int importEdges(Path file) {
        requireAdmin();
        int applied = 0;
        for (String line : readLines(file)) {
            if (line.isBlank()) continue;
            List<String> c = Csv.parse(line);
            try {
                int km = Integer.parseInt(c.get(2));
                graph.addNode(c.get(0));
                graph.addNode(c.get(1));
                graph.setEdge(findName(c.get(0)), findName(c.get(1)), km);
                applied++;
            } catch (RuntimeException ignored) {
                // header row or bad line
            }
        }
        storage.saveGraph(graph);
        record("IMPORT_EDGES", applied + " connections");
        fire();
        return applied;
    }

    private String findName(String name) {
        for (String n : graph.nodes()) if (n.equalsIgnoreCase(name.trim())) return n;
        return name.trim();
    }

    public void resetSampleData() {
        requireAdmin();
        SampleData.fillEvents(bst);
        SampleData.fillGraph(graph);
        storage.saveEvents(bst);
        storage.saveGraph(graph);
        record("RESET_DATA", "sample events and campuses restored");
        fire();
    }

    private static void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    // ---------------------------------------------------------------- accounts (admin)

    public void addUser(String username, String displayName, char[] password, Role role) {
        requireAdmin();
        auth.add(username, displayName, password, role);
        storage.saveUsers(auth.users());
        record("ADD_USER", username.trim() + " (" + role.label() + ")");
        fire();
    }

    public void setUserActive(String username, boolean active) {
        requireAdmin();
        if (!active && current.username().equalsIgnoreCase(username)) {
            throw new IllegalStateException("You cannot disable your own account");
        }
        auth.setActive(username, active);
        storage.saveUsers(auth.users());
        record(active ? "ENABLE_USER" : "DISABLE_USER", username);
        fire();
    }

    public void resetPassword(String username, char[] password) {
        requireAdmin();
        auth.resetPassword(username, password);
        storage.saveUsers(auth.users());
        record("RESET_PASSWORD", username);
        fire();
    }

    public void deleteUser(String username) {
        requireAdmin();
        if (current.username().equalsIgnoreCase(username)) throw new IllegalStateException("You cannot delete your own account");
        auth.delete(username);
        storage.saveUsers(auth.users());
        record("DELETE_USER", username);
        fire();
    }
}


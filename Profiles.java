package lspu_hnn_core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Extra sign-up details (student ID, email, campus) kept in profiles.csv next to the other data files.
 * It also lets people sign in with their student ID or email instead of the account ID.
 */
public class Profiles {

    public record Profile(String username, String studentId, String email, String campus) {
    }

    private final Path file;
    private final List<Profile> list = new ArrayList<>();

    public Profiles(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                List<String> c = Csv.parse(line);
                if (c.size() >= 4 && !c.get(0).equalsIgnoreCase("username")) {
                    list.add(new Profile(c.get(0), c.get(1), c.get(2), c.get(3)));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add("username,studentId,email,campus");
        for (Profile p : list) {
            lines.add(Csv.quote(p.username()) + "," + Csv.quote(p.studentId()) + ","
                    + Csv.quote(p.email()) + "," + Csv.quote(p.campus()));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    public void add(Profile p) {
        list.add(p);
        save();
    }

    public Profile byUsername(String username) {
        for (Profile p : list) if (p.username().equalsIgnoreCase(username)) return p;
        return null;
    }

    public boolean studentIdTaken(String studentId) {
        for (Profile p : list) if (p.studentId().equalsIgnoreCase(studentId)) return true;
        return false;
    }

    public boolean emailTaken(String email) {
        for (Profile p : list) if (p.email().toLowerCase(Locale.ROOT).equals(email.toLowerCase(Locale.ROOT))) return true;
        return false;
    }

    /** Account ID for a typed student ID or email, or null if nothing matches. */
    public String usernameFor(String typed) {
        for (Profile p : list) {
            if (p.studentId().equalsIgnoreCase(typed) || p.email().equalsIgnoreCase(typed)) return p.username();
        }
        return null;
    }
}
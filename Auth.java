package lspu_hnn_core;


import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Login checking and account management. Passwords are hashed with PBKDF2 + a random salt. */
public class Auth {

    public static class AuthException extends Exception {
        private static final long serialVersionUID = 1L;

        public AuthException(String message) {
            super(message);
        }
    }

    public static final int MAX_FAILS = 3;
    public static final int LOCK_SECONDS = 30;
    private static final int ITERATIONS = 100_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final List<User> users = new ArrayList<>();
    private final Map<String, Integer> fails = new HashMap<>();
    private final Map<String, Long> lockedUntil = new HashMap<>();

    // ---------------------------------------------------------------- setup

    public List<User> users() {
        return Collections.unmodifiableList(users);
    }

    void setUsers(List<User> loaded) {
        users.clear();
        users.addAll(loaded);
    }

    /** Demo accounts used on first run. Listed in the README. */
    void seed() {
        users.clear();
        users.add(make("admin", "Administrator", "admin123".toCharArray(), Role.ADMIN));
        users.add(make("user", "Student User", "user123".toCharArray(), Role.USER));
    }

    // ---------------------------------------------------------------- login

    public User authenticate(String username, char[] password) throws AuthException {
        String key = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        long remain = lockRemainingMs(key);
        if (remain > 0) {
            throw new AuthException("Too many attempts. Try again in " + ((remain + 999) / 1000) + " seconds.");
        }
        User u = find(key);
        boolean ok = u != null && verify(password, u);
        if (!ok) {
            int count = fails.merge(key, 1, Integer::sum);
            if (count >= MAX_FAILS) {
                fails.remove(key);
                lockedUntil.put(key, System.currentTimeMillis() + LOCK_SECONDS * 1000L);
                throw new AuthException("Too many attempts. Try again in " + LOCK_SECONDS + " seconds.");
            }
            throw new AuthException("Wrong username or password");
        }
        fails.remove(key);
        if (!u.active) throw new AuthException("This account is disabled. Ask an admin.");
        return u;
    }

    public long lockRemainingMs(String username) {
        Long until = lockedUntil.get(username.trim().toLowerCase(Locale.ROOT));
        if (until == null) return 0;
        long left = until - System.currentTimeMillis();
        if (left <= 0) {
            lockedUntil.remove(username.trim().toLowerCase(Locale.ROOT));
            return 0;
        }
        return left;
    }

    // ---------------------------------------------------------------- account management

    public User add(String username, String displayName, char[] password, Role role) {
        username = username == null ? "" : username.trim();
        if (!username.matches("[A-Za-z0-9_.@-]{3,40}")) {
            throw new IllegalArgumentException("ID must be 3-40 letters, numbers, dots, @, _ or -");
        }
        if (find(username.toLowerCase(Locale.ROOT)) != null) throw new IllegalArgumentException("That username is already taken");
        checkPassword(password);
        String name = displayName == null || displayName.isBlank() ? username : displayName.trim();
        User u = make(username, name, password, role);
        users.add(u);
        return u;
    }

    public void setActive(String username, boolean active) {
        User u = require(username);
        if (!active && u.role == Role.ADMIN && u.active && activeAdmins() <= 1) {
            throw new IllegalStateException("At least one active admin must remain");
        }
        u.active = active;
    }

    public void resetPassword(String username, char[] password) {
        User u = require(username);
        checkPassword(password);
        byte[] salt = newSalt();
        u.salt = HexFormat.of().formatHex(salt);
        u.hash = hash(password, salt);
    }

    public void delete(String username) {
        User u = require(username);
        if (u.role == Role.ADMIN && u.active && activeAdmins() <= 1) {
            throw new IllegalStateException("At least one active admin must remain");
        }
        users.remove(u);
    }

    // ---------------------------------------------------------------- helpers

    private int activeAdmins() {
        int n = 0;
        for (User u : users) if (u.role == Role.ADMIN && u.active) n++;
        return n;
    }

    private User find(String lowerName) {
        for (User u : users) if (u.username.toLowerCase(Locale.ROOT).equals(lowerName)) return u;
        return null;
    }

    private User require(String username) {
        User u = find(username.toLowerCase(Locale.ROOT));
        if (u == null) throw new IllegalArgumentException("No such account: " + username);
        return u;
    }

    private static void checkPassword(char[] pw) {
        if (pw == null || pw.length < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
    }

    private static User make(String username, String display, char[] password, Role role) {
        byte[] salt = newSalt();
        return new User(username, display, HexFormat.of().formatHex(salt), hash(password, salt), role, true);
    }

    private static byte[] newSalt() {
        byte[] s = new byte[16];
        RANDOM.nextBytes(s);
        return s;
    }

    static String hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, 256);
        try {
            byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return HexFormat.of().formatHex(h);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean verify(char[] password, User u) {
        String h = hash(password, HexFormat.of().parseHex(u.salt));
        return MessageDigest.isEqual(h.getBytes(), u.hash.getBytes());
    }
}


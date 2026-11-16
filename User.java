package lspu_hnn_core;


/** An account. The password is never stored, only a salted PBKDF2 hash. */
public final class User {
    final String username;
    String displayName;
    String salt;      // hex
    String hash;      // hex
    Role role;
    boolean active;

    User(String username, String displayName, String salt, String hash, Role role, boolean active) {
        this.username = username;
        this.displayName = displayName;
        this.salt = salt;
        this.hash = hash;
        this.role = role;
        this.active = active;
    }

    public String username() {
        return username;
    }

    public String displayName() {
        return displayName;
    }

    public Role role() {
        return role;
    }

    public boolean active() {
        return active;
    }
}

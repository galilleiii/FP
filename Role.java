package lspu_hnn_core;


public enum Role {
    ADMIN, USER;

    public String label() {
        return this == ADMIN ? "Admin" : "User";
    }
}


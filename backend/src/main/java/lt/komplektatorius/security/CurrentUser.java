package lt.komplektatorius.security;

import java.util.UUID;
import lt.komplektatorius.auth.Role;

public record CurrentUser(UUID id, String email, Role role) {
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isEditor() { return role == Role.EDITOR || role == Role.ADMIN; }
}


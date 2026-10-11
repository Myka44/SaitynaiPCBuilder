package lt.komplektatorius.auth.dto;

import java.util.UUID;
import lt.komplektatorius.auth.Role;

public record UserResponse(UUID id, String email, Role role) {
}


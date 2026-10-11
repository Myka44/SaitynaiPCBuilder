package lt.komplektatorius.security;

import java.util.Optional;
import java.util.UUID;
import lt.komplektatorius.auth.Role;
import lt.komplektatorius.common.error.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {
    public Optional<CurrentUser> optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt) || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String userId = jwt.getToken().getClaimAsString("userId");
        String email = jwt.getToken().getClaimAsString("email");
        String role = jwt.getToken().getClaimAsString("role");
        return Optional.of(new CurrentUser(UUID.fromString(userId), email, Role.valueOf(role)));
    }

    public CurrentUser required() {
        return optional().orElseThrow(() -> new UnauthorizedException("Prisijunkite ir pakartokite veiksmą."));
    }
}


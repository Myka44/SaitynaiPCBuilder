package lt.komplektatorius.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import lt.komplektatorius.auth.dto.AuthResponse;
import lt.komplektatorius.auth.dto.LoginRequest;
import lt.komplektatorius.auth.dto.RegisterRequest;
import lt.komplektatorius.auth.dto.UserResponse;
import lt.komplektatorius.common.error.ConflictException;
import lt.komplektatorius.common.error.UnauthorizedException;
import lt.komplektatorius.security.CurrentUserService;
import lt.komplektatorius.security.JwtProperties;
import lt.komplektatorius.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties properties;
    private final CurrentUserService currentUserService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       JwtProperties properties, CurrentUserService currentUserService) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TokenPair register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Naudotojas tokiu el. paštu jau egzistuoja.");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.MEMBER);
        users.save(user);
        return issue(user, UUID.randomUUID());
    }

    @Transactional
    public TokenPair login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(this::badCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw badCredentials();
        }
        return issue(user, UUID.randomUUID());
    }

    @Transactional
    public TokenPair refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw badRefreshToken();
        RefreshToken old = refreshTokens.findByTokenHash(hash(rawToken)).orElseThrow(this::badRefreshToken);
        Instant now = Instant.now();
        if (old.getRevokedAt() != null) {
            refreshTokens.revokeFamily(old.getFamilyId(), now);
            throw badRefreshToken();
        }
        if (old.getExpiresAt().isBefore(now)) {
            old.setRevokedAt(now);
            throw badRefreshToken();
        }
        old.setRevokedAt(now);
        TokenPair pair = issue(old.getUser(), old.getFamilyId());
        old.setReplacedBy(pair.refreshTokenEntity());
        return pair;
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        refreshTokens.findByTokenHash(hash(rawToken))
                .ifPresent(token -> refreshTokens.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        var current = currentUserService.required();
        return new UserResponse(current.id(), current.email(), current.role());
    }

    private TokenPair issue(User user, UUID familyId) {
        String rawRefresh = randomToken();
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setTokenHash(hash(rawRefresh));
        refresh.setFamilyId(familyId);
        refresh.setExpiresAt(Instant.now().plus(properties.refreshTokenDays(), ChronoUnit.DAYS));
        refreshTokens.save(refresh);
        AuthResponse response = new AuthResponse(jwtService.createAccessToken(user), "Bearer",
                jwtService.expiresInSeconds(), new UserResponse(user.getId(), user.getEmail(), user.getRole()));
        return new TokenPair(response, rawRefresh, refresh);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UnauthorizedException badCredentials() {
        return new UnauthorizedException("Neteisingas el. paštas arba slaptažodis.");
    }

    private UnauthorizedException badRefreshToken() {
        return new UnauthorizedException("Refresh tokenas negalioja arba jo galiojimas baigėsi.");
    }

    public record TokenPair(AuthResponse response, String rawRefreshToken, RefreshToken refreshTokenEntity) {}
}


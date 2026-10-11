package lt.komplektatorius.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.Duration;
import lt.komplektatorius.auth.dto.AuthResponse;
import lt.komplektatorius.auth.dto.LoginRequest;
import lt.komplektatorius.auth.dto.RegisterRequest;
import lt.komplektatorius.auth.dto.UserResponse;
import lt.komplektatorius.security.CookieProperties;
import lt.komplektatorius.security.JwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public static final String REFRESH_COOKIE = "refresh_token";
    private final AuthService authService;
    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, CookieProperties cookieProperties, JwtProperties jwtProperties) {
        this.authService = authService;
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    @Operation(summary = "Registruoti naują MEMBER naudotoją")
    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return tokenResponse(authService.register(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Prisijungti el. paštu ir slaptažodžiu")
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return tokenResponse(authService.login(request), HttpStatus.OK);
    }

    @Operation(summary = "Rotuoti refresh tokeną ir gauti naują access tokeną")
    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        return tokenResponse(authService.refresh(refreshToken), HttpStatus.OK);
    }

    @Operation(summary = "Panaikinti dabartinę refresh tokenų šeimą")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie().toString()).build();
    }

    @Operation(summary = "Grąžinti dabartinį naudotoją", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(authService.me());
    }

    private ResponseEntity<AuthResponse> tokenResponse(AuthService.TokenPair pair, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshCookie(pair.rawRefreshToken()).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pair.response());
    }

    private ResponseCookie refreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(jwtProperties.refreshTokenDays()))
                .build();
    }

    private ResponseCookie clearCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}

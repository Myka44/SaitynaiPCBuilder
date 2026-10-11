package lt.komplektatorius.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lt.komplektatorius.auth.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .audience(java.util.List.of(properties.audience()))
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES))
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return properties.accessTokenMinutes() * 60;
    }
}


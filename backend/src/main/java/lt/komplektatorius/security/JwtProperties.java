package lt.komplektatorius.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, String issuer, String audience,
                            long accessTokenMinutes, long refreshTokenDays) {
}


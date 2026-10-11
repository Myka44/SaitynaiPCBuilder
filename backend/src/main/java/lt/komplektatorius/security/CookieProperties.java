package lt.komplektatorius.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.cookie")
public record CookieProperties(boolean secure, String sameSite) {
}


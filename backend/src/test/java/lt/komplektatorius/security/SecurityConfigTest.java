package lt.komplektatorius.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import lt.komplektatorius.common.web.SpaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SecurityConfig.class, SecurityConfigTest.TestConfig.class, SpaController.class})
@TestPropertySource(properties = "app.allowed-origin=http://localhost:5173")
class SecurityConfigTest {
    @Autowired WebApplicationContext context;
    @Autowired JwtEncoder encoder;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void acceptsBase64KeyWithoutChangingItsBytes() {
        byte[] bytes = new byte[32];
        bytes[0] = 42;
        assertThat(new SecurityConfig().jwtSecretKey(properties(Base64.getEncoder().encodeToString(bytes)))
                .getEncoded()).isEqualTo(bytes);
    }

    @Test
    void rejectsMissingMalformedAndShortSecrets() {
        for (String secret : new String[] {null, "", "   ", "raw-secret-with-hyphens-is-not-base64",
                Base64.getEncoder().encodeToString(new byte[31])}) {
            assertThatIllegalStateException().isThrownBy(
                    () -> new SecurityConfig().jwtSecretKey(properties(secret)));
        }
    }

    @Test
    void missingAndInvalidTokensReturnTheSameProblemResponse() throws Exception {
        mvc.perform(get("/security-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("status").value(401))
                .andExpect(jsonPath("detail").value("Pateikite galiojantį access tokeną."));
        mvc.perform(get("/security-test").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("detail").value("Pateikite galiojantį access tokeną."));
    }

    @Test
    void publicReadsRemainAvailableWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/components/security-test")).andExpect(status().isOk());
    }

    @Test
    void browserRoutesArePublicAndForwardToReact() throws Exception {
        for (String path : List.of("/", "/login", "/register", "/forbidden", "/catalog", "/catalog/example",
                "/configurations/example", "/app/configurations", "/app/configurations/example/edit",
                "/admin/configurations")) {
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                            .forwardedUrl("/index.html"));
        }
    }

    @Test
    void protectedApiIsNotForwardedToReact() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/v1/configurations")).andExpect(status().isUnauthorized());
    }

    @Test
    void missingAssetsAndPublicApiRoutesDoNotReturnTheReactPage() throws Exception {
        mvc.perform(get("/assets/missing.js")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/components/missing")).andExpect(status().isNotFound());
    }

    @Test
    void memberIsForbiddenButEditorAndAdminPassEditorCheck() throws Exception {
        mvc.perform(get("/security-test").header("Authorization", "Bearer " + token("MEMBER")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("status").value(403));
        for (String role : List.of("EDITOR", "ADMIN")) {
            mvc.perform(get("/security-test").header("Authorization", "Bearer " + token(role)))
                    .andExpect(status().isOk());
        }
    }

    private String token(String role) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("test-api").audience(List.of("test-web"))
                .subject("test-user").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300))
                .claim("role", role).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private static JwtProperties properties(String secret) {
        return new JwtProperties(secret, "test-api", "test-web", 15, 7);
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    static class TestConfig {
        @Bean
        JwtProperties jwtProperties() {
            return properties(Base64.getEncoder().encodeToString(new byte[32]));
        }

        @Bean
        OriginValidationFilter originValidationFilter() {
            return new OriginValidationFilter("http://localhost:5173");
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/security-test")
        @PreAuthorize("hasRole('EDITOR')")
        public String secured() {
            return "ok";
        }

        @GetMapping("/api/v1/components/security-test")
        public String publicRead() {
            return "ok";
        }
    }
}

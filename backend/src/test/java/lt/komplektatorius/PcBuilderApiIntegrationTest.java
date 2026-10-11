package lt.komplektatorius;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PcBuilderApiIntegrationTest {
    private static final String MEMBER_EMAIL = "narys@example.lt";
    private static final String MEMBER_PASSWORD = "Password1";
    private static final Pattern ACCESS_TOKEN = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("pc_builder_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-at-least-32-bytes");
        registry.add("app.allowed-origin", () -> "http://localhost:5173");
        registry.add("app.cookie.secure", () -> false);
    }

    @LocalServerPort
    int port;

    HttpClient client;

    @BeforeEach
    void clientWithCookies() {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Test
    void migrationsPublicFilteringAndHalLinksWork() throws Exception {
        HttpResponse<String> health = get("/actuator/health", null);
        HttpResponse<String> components = get("/api/v1/components?size=2&sort=name,asc", null);
        HttpResponse<String> publicConfigurations = get("/api/v1/configurations?size=100", null);
        HttpResponse<String> privateConfiguration = get(
                "/api/v1/configurations/30000000-0000-0000-0000-000000000002", null);
        HttpResponse<String> openApi = get("/v3/api-docs", null);

        assertThat(health.statusCode()).isEqualTo(200);
        assertThat(health.body()).contains("\"status\":\"UP\"");
        assertThat(components.statusCode()).isEqualTo(200);
        assertThat(components.headers().firstValue("content-type").orElse("")).contains("application/hal+json");
        assertThat(components.body()).contains("\"totalElements\":10", "\"self\"");
        assertThat(publicConfigurations.body()).contains("\"totalElements\":4");
        assertThat(privateConfiguration.statusCode()).isEqualTo(404);
        assertThat(openApi.statusCode()).isEqualTo(200);
        assertThat(openApi.body()).contains("\"bearerAuth\"", "\"ProblemDetail\"", "\"409\"");
    }

    @Test
    void memberCanLoginRefreshAndManageOwnConfiguration() throws Exception {
        HttpResponse<String> login = post("/api/v1/auth/login",
                "{\"email\":\"" + MEMBER_EMAIL + "\",\"password\":\"" + MEMBER_PASSWORD + "\"}", null, null);
        String token = accessToken(login.body());
        HttpResponse<String> ownPrivate = get(
                "/api/v1/configurations/30000000-0000-0000-0000-000000000002", token);
        HttpResponse<String> forbiddenCatalogWrite = post("/api/v1/components", """
                {"name":"Test component","category":"CPU","manufacturer":"Demo","model":"X1",
                 "description":"Only an authorization test","powerWatts":65}
                """, token, null);
        HttpResponse<String> created = post("/api/v1/configurations", """
                {"name":"Integracinis testas","description":"Laikina testinė komplektacija",
                 "budgetEur":1500,"usageType":"GAMING","visibility":"PRIVATE"}
                """, token, null);

        assertThat(login.statusCode()).isEqualTo(200);
        assertThat(login.headers().firstValue("set-cookie").orElse("")).contains("HttpOnly", "Path=/api/v1/auth");
        assertThat(ownPrivate.statusCode()).isEqualTo(200);
        assertThat(forbiddenCatalogWrite.statusCode()).isEqualTo(403);
        assertThat(forbiddenCatalogWrite.body()).contains("\"status\":403", "\"title\":\"Veiksmas neleidžiamas\"");
        assertThat(created.statusCode()).isEqualTo(201);
        String location = created.headers().firstValue("location").orElseThrow();

        HttpResponse<String> refreshed = post("/api/v1/auth/refresh", "{}", null, "http://localhost:5173");
        String refreshedToken = accessToken(refreshed.body());
        HttpResponse<String> deleted = delete(URI.create(location).getPath(), refreshedToken);

        assertThat(refreshed.statusCode()).isEqualTo(200);
        assertThat(refreshedToken).isNotEqualTo(token);
        assertThat(deleted.statusCode()).isEqualTo(204);
    }

    @Test
    void validatesQueriesAndThreeLevelHierarchy() throws Exception {
        HttpResponse<String> badPage = get("/api/v1/components?size=101", null);
        HttpResponse<String> validHierarchy = get("/api/v1/configurations/30000000-0000-0000-0000-000000000001"
                + "/components/10000000-0000-0000-0000-000000000001/offers", null);
        HttpResponse<String> wrongHierarchy = get("/api/v1/configurations/30000000-0000-0000-0000-000000000001"
                + "/components/10000000-0000-0000-0000-000000000002/offers", null);
        HttpResponse<String> summary = get("/api/v1/configurations/30000000-0000-0000-0000-000000000001/summary", null);

        assertThat(badPage.statusCode()).isEqualTo(400);
        assertThat(badPage.body()).contains("\"title\":\"Netinkama užklausa\"");
        assertThat(validHierarchy.statusCode()).isEqualTo(200);
        assertThat(validHierarchy.body()).contains("BalticPC Demo", "TechTurgus Demo");
        assertThat(wrongHierarchy.statusCode()).isEqualTo(404);
        assertThat(summary.statusCode()).isEqualTo(200);
        assertThat(summary.body()).contains("\"totalLowestPriceEur\"", "\"withinBudget\"", "\"components\"");

        HttpResponse<String> wrongOrigin = post("/api/v1/auth/refresh", "{}", null, "https://evil.example");
        assertThat(wrongOrigin.statusCode()).isEqualTo(403);
        assertThat(wrongOrigin.body()).contains("\"status\":403", "Užklausos kilmė neleidžiama");
    }

    private HttpResponse<String> get(String path, String bearer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path)).GET();
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json, String bearer, String origin) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        if (origin != null) request.header("Origin", origin);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String bearer) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Authorization", "Bearer " + bearer)
                .DELETE().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static String accessToken(String body) {
        Matcher matcher = ACCESS_TOKEN.matcher(body);
        assertThat(matcher.find()).as("atsakyme yra access tokenas").isTrue();
        return matcher.group(1);
    }
}

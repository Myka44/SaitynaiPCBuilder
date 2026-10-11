package lt.komplektatorius.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

final class SecurityProblemWriter {
    private SecurityProblemWriter() {}

    static void write(HttpServletRequest request, HttpServletResponse response, int status,
                      String title, String detail) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{" +
                "\"type\":\"https://pc-builder.example/problems/" + status + "\"," +
                "\"title\":\"" + title + "\"," +
                "\"status\":" + status + "," +
                "\"detail\":\"" + detail + "\"," +
                "\"instance\":\"" + request.getRequestURI() + "\"}");
    }
}

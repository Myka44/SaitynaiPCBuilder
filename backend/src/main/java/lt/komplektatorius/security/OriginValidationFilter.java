package lt.komplektatorius.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OriginValidationFilter extends OncePerRequestFilter {
    private final String allowedOrigin;

    public OriginValidationFilter(@Value("${app.allowed-origin}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !(path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/logout"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.equals(allowedOrigin)) {
            SecurityProblemWriter.write(request, response, HttpServletResponse.SC_FORBIDDEN,
                    "Veiksmas neleidžiamas", "Užklausos kilmė neleidžiama.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}

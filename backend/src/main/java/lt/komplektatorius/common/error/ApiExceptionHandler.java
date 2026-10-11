package lt.komplektatorius.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(NotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resursas nerastas", ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ProblemDetail> forbidden(ForbiddenException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Veiksmas neleidžiamas", ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ProblemDetail> unauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Reikia prisijungti", ex.getMessage(), request);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ResponseEntity<ProblemDetail> conflict(Exception ex, HttpServletRequest request) {
        String detail = ex instanceof ConflictException ? ex.getMessage() : "Duomenys konfliktuoja su esama būsena.";
        return problem(HttpStatus.CONFLICT, "Duomenų konfliktas", detail, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail detail = base(HttpStatus.BAD_REQUEST, "Netinkami duomenys",
                "Patikrinkite pažymėtus laukus.", request);
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, IllegalArgumentException.class})
    ResponseEntity<ProblemDetail> badRequest(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Netinkama užklausa", "Užklausos formatas arba reikšmė netinkama.", request);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(status).body(base(status, title, detail, request));
    }

    private ProblemDetail base(HttpStatus status, String title, String message, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setType(URI.create("https://pc-builder.example/problems/" + status.value()));
        detail.setInstance(URI.create(request.getRequestURI()));
        return detail;
    }
}


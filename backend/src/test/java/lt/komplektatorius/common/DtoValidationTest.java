package lt.komplektatorius.common;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.Instant;
import lt.komplektatorius.auth.dto.RegisterRequest;
import lt.komplektatorius.offer.StockStatus;
import lt.komplektatorius.offer.dto.OfferRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rejectsHttpOfferUrl() {
        OfferRequest request = new OfferRequest("Tiekėjas Demo", BigDecimal.TEN,
                "http://example.invalid/offer", StockStatus.IN_STOCK, Instant.now());

        assertThat(validator.validate(request)).extracting("propertyPath").extracting(Object::toString)
                .contains("productUrl");
    }

    @Test
    void requiresStrongRegistrationPassword() {
        assertThat(validator.validate(new RegisterRequest("naujas@example.lt", "password")))
                .extracting("propertyPath").extracting(Object::toString).contains("password");
        assertThat(validator.validate(new RegisterRequest("naujas@example.lt", "Password1"))).isEmpty();
    }
}

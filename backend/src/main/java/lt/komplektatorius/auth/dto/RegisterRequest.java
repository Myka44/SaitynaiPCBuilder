package lt.komplektatorius.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(
        @Schema(example = "naujas@example.lt") @NotBlank @Email String email,
        @Schema(example = "Password1")
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$",
                message = "Slaptažodis turi būti 8–72 simbolių ir turėti mažąją, didžiąją raidę bei skaičių")
        String password) {
}

package lt.komplektatorius.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(example = "narys@example.lt") @NotBlank @Email String email,
        @Schema(example = "Password1") @NotBlank String password) {
}

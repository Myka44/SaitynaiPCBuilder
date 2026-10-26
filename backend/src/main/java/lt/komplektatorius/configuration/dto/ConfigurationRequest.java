package lt.komplektatorius.configuration.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lt.komplektatorius.configuration.UsageType;
import lt.komplektatorius.configuration.Visibility;

public record ConfigurationRequest(
        @Schema(example = "Subalansuotas 1440p žaidimų PC")
        @NotBlank @Size(min = 3, max = 100) String name,
        @Schema(example = "Komplektacija žaidimams ir kasdieniam darbui.")
        @NotBlank(message = "Įveskite aprašymą.")
        @Size(max = 1000, message = "Aprašymas negali viršyti 1000 simbolių.") String description,
        @Schema(example = "1700.00")
        @NotNull @DecimalMin("1.00") @DecimalMax("100000.00") BigDecimal budgetEur,
        @Schema(example = "GAMING") @NotNull UsageType usageType,
        @Schema(example = "PUBLIC") @NotNull Visibility visibility) {
}

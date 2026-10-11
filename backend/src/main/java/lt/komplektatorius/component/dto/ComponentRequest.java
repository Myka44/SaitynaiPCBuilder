package lt.komplektatorius.component.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lt.komplektatorius.component.ComponentCategory;

public record ComponentRequest(
        @Schema(example = "Ryzen 7 procesorius") @NotBlank @Size(max = 120) String name,
        @Schema(example = "CPU") @NotNull ComponentCategory category,
        @Schema(example = "AMD") @NotBlank @Size(max = 80) String manufacturer,
        @Schema(example = "Ryzen 7 7700") @NotBlank @Size(max = 100) String model,
        @Schema(example = "8 branduolių procesorius žaidimams ir produktyvumui.")
        @NotBlank @Size(max = 1000) String description,
        @Schema(example = "65", nullable = true) @Min(0) @Max(2000) Integer powerWatts) {
}

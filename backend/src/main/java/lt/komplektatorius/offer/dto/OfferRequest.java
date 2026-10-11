package lt.komplektatorius.offer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lt.komplektatorius.offer.StockStatus;

public record OfferRequest(
        @Schema(example = "BalticPC Demo") @NotBlank @Size(max = 100) String supplierName,
        @Schema(example = "319.99") @NotNull @DecimalMin("0.01") BigDecimal priceEur,
        @Schema(example = "https://example.invalid/ryzen-7700-balticpc")
        @NotBlank @Size(max = 500)
        @Pattern(regexp = "^https://.+", message = "Leidžiami tik https adresai") String productUrl,
        @Schema(example = "IN_STOCK") @NotNull StockStatus stockStatus,
        @Schema(example = "2026-09-07T10:00:00Z") @NotNull Instant checkedAt) {
}

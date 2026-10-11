package lt.komplektatorius.offer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lt.komplektatorius.offer.StockStatus;

public record OfferResponse(UUID id, UUID componentId, String supplierName, BigDecimal priceEur,
                            String productUrl, StockStatus stockStatus, Instant checkedAt,
                            Instant createdAt, Instant updatedAt) {
}


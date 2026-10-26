package lt.komplektatorius.component.dto;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;
import lt.komplektatorius.component.ComponentCategory;

public record ComponentResponse(UUID id, String name, ComponentCategory category, String manufacturer,
                                String model, String description, Integer powerWatts,
                                Instant createdAt, Instant updatedAt, CatalogOffer cheapestOffer, Integer offerCount) {
    public ComponentResponse(UUID id, String name, ComponentCategory category, String manufacturer,
                             String model, String description, Integer powerWatts, Instant createdAt, Instant updatedAt) {
        this(id, name, category, manufacturer, model, description, powerWatts, createdAt, updatedAt, null, null);
    }

    public record CatalogOffer(BigDecimal priceEur, String supplierName) {}
}


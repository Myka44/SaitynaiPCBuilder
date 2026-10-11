package lt.komplektatorius.configuration.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lt.komplektatorius.configuration.UsageType;
import lt.komplektatorius.configuration.Visibility;

public record ConfigurationResponse(UUID id, UUID ownerId, String name, String description,
                                    BigDecimal budgetEur, UsageType usageType, Visibility visibility,
                                    int componentCount, Instant createdAt, Instant updatedAt) {
}


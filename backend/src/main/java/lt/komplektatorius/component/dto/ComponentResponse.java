package lt.komplektatorius.component.dto;

import java.time.Instant;
import java.util.UUID;
import lt.komplektatorius.component.ComponentCategory;

public record ComponentResponse(UUID id, String name, ComponentCategory category, String manufacturer,
                                String model, String description, Integer powerWatts,
                                Instant createdAt, Instant updatedAt) {
}


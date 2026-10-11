package lt.komplektatorius.configuration.dto;

import java.math.BigDecimal;
import java.util.List;
import lt.komplektatorius.component.dto.ComponentResponse;
import lt.komplektatorius.offer.dto.OfferResponse;

public record ConfigurationSummaryResponse(
        ConfigurationResponse configuration,
        List<SummaryComponent> components,
        BigDecimal totalLowestPriceEur,
        int componentsWithoutOffers,
        BigDecimal remainingBudgetEur,
        boolean withinBudget) {
    public record SummaryComponent(ComponentResponse component, OfferResponse cheapestOffer) {}
}


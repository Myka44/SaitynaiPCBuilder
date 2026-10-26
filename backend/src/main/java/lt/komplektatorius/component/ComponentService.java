package lt.komplektatorius.component;

import java.util.Set;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lt.komplektatorius.common.PageRequestFactory;
import lt.komplektatorius.common.error.ConflictException;
import lt.komplektatorius.common.error.NotFoundException;
import lt.komplektatorius.component.dto.ComponentRequest;
import lt.komplektatorius.component.dto.ComponentResponse;
import lt.komplektatorius.configuration.BuildConfigurationRepository;
import lt.komplektatorius.offer.Offer;
import lt.komplektatorius.offer.OfferRepository;
import lt.komplektatorius.offer.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComponentService {
    private static final Set<String> SORT_FIELDS = Set.of("name", "category", "manufacturer", "model",
            "powerWatts", "createdAt", "updatedAt");
    private final ComponentRepository components;
    private final BuildConfigurationRepository configurations;
    private final OfferRepository offers;

    public ComponentService(ComponentRepository components, BuildConfigurationRepository configurations,
                            OfferRepository offers) {
        this.components = components;
        this.configurations = configurations;
        this.offers = offers;
    }

    @Transactional(readOnly = true)
    public Page<ComponentResponse> list(String q, ComponentCategory category, String manufacturer,
                                        Integer minPowerWatts, Integer maxPowerWatts, String supplier,
                                        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPowerWatts != null && maxPowerWatts != null && minPowerWatts > maxPowerWatts) {
            throw new IllegalArgumentException("Minimali galia negali viršyti maksimalios.");
        }
        if ((minPrice != null && minPrice.signum() < 0) || (maxPrice != null && maxPrice.signum() < 0)
                || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)) {
            throw new IllegalArgumentException("Netinkamas kainų intervalas.");
        }
        var page = components.findAll(ComponentSpecifications.filtered(q, category, manufacturer,
                        minPowerWatts, maxPowerWatts).and(ComponentSpecifications.withOffer(supplier, minPrice, maxPrice)),
                PageRequestFactory.normalize(pageable, SORT_FIELDS));
        if (page.isEmpty()) return page.map(this::toResponse);
        Map<UUID, List<Offer>> matchingOffers = offers.findByComponentIdInOrderByPriceEurAsc(
                        page.getContent().stream().map(Component::getId).toList()).stream()
                .filter(offer -> offer.getStockStatus() != StockStatus.OUT_OF_STOCK)
                .filter(offer -> supplier == null || supplier.isBlank()
                        || offer.getSupplierName().equalsIgnoreCase(supplier.trim()))
                .filter(offer -> minPrice == null || offer.getPriceEur().compareTo(minPrice) >= 0)
                .filter(offer -> maxPrice == null || offer.getPriceEur().compareTo(maxPrice) <= 0)
                .collect(Collectors.groupingBy(offer -> offer.getComponent().getId()));
        return page.map(component -> {
            List<Offer> available = matchingOffers.getOrDefault(component.getId(), List.of());
            Offer cheapest = available.isEmpty() ? null : available.getFirst();
            return new ComponentResponse(component.getId(), component.getName(), component.getCategory(),
                    component.getManufacturer(), component.getModel(), component.getDescription(), component.getPowerWatts(),
                    component.getCreatedAt(), component.getUpdatedAt(), cheapest == null ? null
                    : new ComponentResponse.CatalogOffer(cheapest.getPriceEur(), cheapest.getSupplierName()), available.size());
        });
    }

    @Transactional(readOnly = true)
    public List<String> suppliers() { return offers.findSupplierNames(); }

    @Transactional(readOnly = true)
    public ComponentResponse get(UUID id) { return toResponse(require(id)); }

    @Transactional
    public ComponentResponse create(ComponentRequest request) {
        Component component = new Component();
        apply(component, request);
        return toResponse(components.save(component));
    }

    @Transactional
    public ComponentResponse update(UUID id, ComponentRequest request) {
        Component component = require(id);
        apply(component, request);
        return toResponse(component);
    }

    @Transactional
    public void delete(UUID id) {
        Component component = require(id);
        if (configurations.countByComponentsId(id) > 0) {
            throw new ConflictException("Komponentas naudojamas bent vienoje komplektacijoje.");
        }
        components.delete(component);
    }

    public Component require(UUID id) {
        return components.findById(id).orElseThrow(() -> new NotFoundException("Komponentas nerastas."));
    }

    public ComponentResponse toResponse(Component c) {
        return new ComponentResponse(c.getId(), c.getName(), c.getCategory(), c.getManufacturer(), c.getModel(),
                c.getDescription(), c.getPowerWatts(), c.getCreatedAt(), c.getUpdatedAt());
    }

    private void apply(Component c, ComponentRequest request) {
        c.setName(request.name().trim());
        c.setCategory(request.category());
        c.setManufacturer(request.manufacturer().trim());
        c.setModel(request.model().trim());
        c.setDescription(request.description().trim());
        c.setPowerWatts(request.powerWatts());
    }
}

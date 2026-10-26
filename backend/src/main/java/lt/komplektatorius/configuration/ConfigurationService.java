package lt.komplektatorius.configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lt.komplektatorius.auth.User;
import lt.komplektatorius.auth.UserRepository;
import lt.komplektatorius.common.PageRequestFactory;
import lt.komplektatorius.common.error.ForbiddenException;
import lt.komplektatorius.common.error.NotFoundException;
import lt.komplektatorius.common.error.UnauthorizedException;
import lt.komplektatorius.component.Component;
import lt.komplektatorius.component.ComponentService;
import lt.komplektatorius.component.dto.ComponentResponse;
import lt.komplektatorius.configuration.dto.ConfigurationRequest;
import lt.komplektatorius.configuration.dto.ConfigurationResponse;
import lt.komplektatorius.configuration.dto.ConfigurationSummaryResponse;
import lt.komplektatorius.offer.Offer;
import lt.komplektatorius.offer.OfferRepository;
import lt.komplektatorius.offer.OfferService;
import lt.komplektatorius.offer.StockStatus;
import lt.komplektatorius.offer.dto.OfferResponse;
import lt.komplektatorius.security.CurrentUser;
import lt.komplektatorius.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationService {
    private static final Set<String> SORT_FIELDS = Set.of("name", "budgetEur", "usageType", "visibility",
            "createdAt", "updatedAt");
    private final BuildConfigurationRepository configurations;
    private final UserRepository users;
    private final ComponentService componentService;
    private final OfferRepository offers;
    private final OfferService offerService;
    private final CurrentUserService currentUsers;

    public ConfigurationService(BuildConfigurationRepository configurations, UserRepository users,
                                ComponentService componentService, OfferRepository offers,
                                OfferService offerService, CurrentUserService currentUsers) {
        this.configurations = configurations;
        this.users = users;
        this.componentService = componentService;
        this.offers = offers;
        this.offerService = offerService;
        this.currentUsers = currentUsers;
    }

    @Transactional(readOnly = true)
    public Page<ConfigurationResponse> list(String q, Visibility visibility, UsageType usageType,
                                            boolean ownerOnly, Pageable pageable) {
        Optional<CurrentUser> current = currentUsers.optional();
        if (ownerOnly && current.isEmpty()) throw new UnauthorizedException("Prisijunkite savo komplektacijoms matyti.");
        UUID userId = current.map(CurrentUser::id).orElse(null);
        boolean admin = current.map(CurrentUser::isAdmin).orElse(false);
        return configurations.findAll(ConfigurationSpecifications.filtered(q, visibility, usageType, userId,
                        admin, ownerOnly), PageRequestFactory.normalize(pageable, SORT_FIELDS))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ConfigurationResponse get(UUID id) { return toResponse(requireAccessible(id)); }

    @Transactional
    public ConfigurationResponse create(ConfigurationRequest request) {
        CurrentUser current = currentUsers.required();
        User owner = users.findById(current.id()).orElseThrow(() -> new NotFoundException("Naudotojas nerastas."));
        BuildConfiguration configuration = new BuildConfiguration();
        configuration.setOwner(owner);
        apply(configuration, request);
        return toResponse(configurations.save(configuration));
    }

    @Transactional
    public ConfigurationResponse update(UUID id, ConfigurationRequest request) {
        BuildConfiguration configuration = requireAccessible(id);
        requireOwnerOrAdmin(configuration);
        apply(configuration, request);
        return toResponse(configuration);
    }

    @Transactional
    public void delete(UUID id) {
        BuildConfiguration configuration = requireAccessible(id);
        requireOwnerOrAdmin(configuration);
        configurations.delete(configuration);
    }

    @Transactional
    public void attachComponent(UUID configurationId, UUID componentId) {
        BuildConfiguration configuration = requireAccessibleWithComponents(configurationId);
        requireOwnerOrAdmin(configuration);
        configuration.getComponents().add(componentService.require(componentId));
    }

    @Transactional
    public void detachComponent(UUID configurationId, UUID componentId) {
        BuildConfiguration configuration = requireAccessibleWithComponents(configurationId);
        requireOwnerOrAdmin(configuration);
        boolean removed = configuration.getComponents().removeIf(component -> component.getId().equals(componentId));
        if (!removed) throw new NotFoundException("Komponentas nepriklauso šiai komplektacijai.");
    }

    @Transactional(readOnly = true)
    public void assertComponentAccessible(UUID configurationId, UUID componentId) {
        BuildConfiguration configuration = requireAccessibleWithComponents(configurationId);
        boolean attached = configuration.getComponents().stream().anyMatch(component -> component.getId().equals(componentId));
        if (!attached) throw new NotFoundException("Komponentas nepriklauso šiai komplektacijai.");
    }

    @Transactional(readOnly = true)
    public ConfigurationSummaryResponse summary(UUID id) {
        BuildConfiguration configuration = requireAccessibleWithComponents(id);
        List<ConfigurationSummaryResponse.SummaryComponent> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int missing = 0;
        List<Component> sorted = configuration.getComponents().stream()
                .sorted(Comparator.comparing(Component::getCategory).thenComparing(Component::getName)).toList();
        for (Component component : sorted) {
            List<Offer> available = offers.findByComponentIdAndStockStatusInOrderByPriceEurAsc(component.getId(),
                    List.of(StockStatus.IN_STOCK, StockStatus.LOW_STOCK));
            OfferResponse cheapest = available.isEmpty() ? null : offerService.toResponse(available.getFirst());
            if (cheapest == null) {
                missing++;
            }
            else {
                total = total.add(cheapest.priceEur());
            }
            ComponentResponse componentResponse = componentService.toResponse(component);
            rows.add(new ConfigurationSummaryResponse.SummaryComponent(componentResponse, cheapest));
        }
        BigDecimal remaining = configuration.getBudgetEur().subtract(total);
        boolean withinBudget = missing == 0 && remaining.signum() >= 0;
        return new ConfigurationSummaryResponse(toResponse(configuration), rows, total, missing, remaining, withinBudget);
    }

    private BuildConfiguration requireAccessible(UUID id) {
        BuildConfiguration configuration = configurations.findById(id)
                .orElseThrow(() -> new NotFoundException("Komplektacija nerasta."));
        checkReadable(configuration);
        return configuration;
    }

    private BuildConfiguration requireAccessibleWithComponents(UUID id) {
        BuildConfiguration configuration = configurations.findWithComponentsById(id)
                .orElseThrow(() -> new NotFoundException("Komplektacija nerasta."));
        checkReadable(configuration);
        return configuration;
    }

    private void checkReadable(BuildConfiguration configuration) {
        if (configuration.getVisibility() == Visibility.PUBLIC) return;
        Optional<CurrentUser> current = currentUsers.optional();
        if (current.isEmpty()) throw new NotFoundException("Komplektacija nerasta.");
        if (!current.get().isAdmin() && !configuration.getOwner().getId().equals(current.get().id())) {
            throw new NotFoundException("Komplektacija nerasta.");
        }
    }

    private void requireOwnerOrAdmin(BuildConfiguration configuration) {
        CurrentUser current = currentUsers.required();
        if (!current.isAdmin() && !configuration.getOwner().getId().equals(current.id())) {
            throw new ForbiddenException("Galima keisti tik savo komplektacijas.");
        }
    }

    private void apply(BuildConfiguration c, ConfigurationRequest request) {
        c.setName(request.name().trim());
        c.setDescription(request.description().trim());
        c.setBudgetEur(request.budgetEur());
        c.setUsageType(request.usageType());
        c.setVisibility(request.visibility());
    }

    private ConfigurationResponse toResponse(BuildConfiguration c) {
        return new ConfigurationResponse(c.getId(), c.getOwner().getId(), c.getName(), c.getDescription(),
                c.getBudgetEur(), c.getUsageType(), c.getVisibility(), c.getComponents().size(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}


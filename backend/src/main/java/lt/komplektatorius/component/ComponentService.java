package lt.komplektatorius.component;

import java.util.Set;
import java.util.UUID;
import lt.komplektatorius.common.PageRequestFactory;
import lt.komplektatorius.common.error.ConflictException;
import lt.komplektatorius.common.error.NotFoundException;
import lt.komplektatorius.component.dto.ComponentRequest;
import lt.komplektatorius.component.dto.ComponentResponse;
import lt.komplektatorius.configuration.BuildConfigurationRepository;
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

    public ComponentService(ComponentRepository components, BuildConfigurationRepository configurations) {
        this.components = components;
        this.configurations = configurations;
    }

    @Transactional(readOnly = true)
    public Page<ComponentResponse> list(String q, ComponentCategory category, String manufacturer,
                                        Integer minPowerWatts, Integer maxPowerWatts, Pageable pageable) {
        if (minPowerWatts != null && maxPowerWatts != null && minPowerWatts > maxPowerWatts) {
            throw new IllegalArgumentException("Minimali galia negali viršyti maksimalios.");
        }
        return components.findAll(ComponentSpecifications.filtered(q, category, manufacturer,
                        minPowerWatts, maxPowerWatts), PageRequestFactory.normalize(pageable, SORT_FIELDS))
                .map(this::toResponse);
    }

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

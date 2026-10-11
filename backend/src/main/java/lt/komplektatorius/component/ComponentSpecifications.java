package lt.komplektatorius.component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ComponentSpecifications {
    private ComponentSpecifications() {}

    public static Specification<Component> filtered(String query, ComponentCategory category, String manufacturer,
                                             Integer minPowerWatts, Integer maxPowerWatts) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("model")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (category != null){
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (manufacturer != null && !manufacturer.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("manufacturer")), manufacturer.trim().toLowerCase(Locale.ROOT)));
            }
            if (minPowerWatts != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("powerWatts"), minPowerWatts));
            }
            if (maxPowerWatts != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("powerWatts"), maxPowerWatts));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}


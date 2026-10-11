package lt.komplektatorius.configuration;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class ConfigurationSpecifications {
    private ConfigurationSpecifications() {}

    static Specification<BuildConfiguration> filtered(String query, Visibility visibility, UsageType usageType,
                                                       UUID userId, boolean admin, boolean ownerOnly) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId == null) {
                predicates.add(cb.equal(root.get("visibility"), Visibility.PUBLIC));
            }
            else if (ownerOnly) {
                predicates.add(cb.equal(root.get("owner").get("id"), userId));
            }
            else if (!admin) {
                predicates.add(cb.or(
                        cb.equal(root.get("visibility"), Visibility.PUBLIC),
                        cb.equal(root.get("owner").get("id"), userId)));
            }
            if (visibility != null) {
                predicates.add(cb.equal(root.get("visibility"), visibility));
            }
            if (usageType != null) {
                predicates.add(cb.equal(root.get("usageType"), usageType));
            }
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}


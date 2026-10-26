package lt.komplektatorius.component;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import lt.komplektatorius.offer.Offer;
import lt.komplektatorius.offer.StockStatus;

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

    public static Specification<Component> withOffer(String supplier, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if ((supplier == null || supplier.isBlank()) && minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            var offers = query.subquery(Integer.class);
            var offer = offers.from(Offer.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(offer.get("component"), root));
            predicates.add(cb.notEqual(offer.get("stockStatus"), StockStatus.OUT_OF_STOCK));
            if (supplier != null && !supplier.isBlank()) {
                predicates.add(cb.equal(cb.lower(offer.get("supplierName")), supplier.trim().toLowerCase(Locale.ROOT)));
            }
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(offer.get("priceEur"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(offer.get("priceEur"), maxPrice));
            offers.select(cb.literal(1)).where(predicates.toArray(Predicate[]::new));
            return cb.exists(offers);
        };
    }
}


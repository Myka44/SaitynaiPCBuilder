package lt.komplektatorius.offer;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class OfferSpecifications {
    private OfferSpecifications() {}

    static Specification<Offer> filtered(UUID componentId, String supplier, StockStatus stockStatus,
                                         BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("component").get("id"), componentId));
            if (supplier != null && !supplier.isBlank()) {
                String like = "%" + supplier.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("supplierName")), like));
            }
            if (stockStatus != null) predicates.add(cb.equal(root.get("stockStatus"), stockStatus));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("priceEur"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("priceEur"), maxPrice));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}


package lt.komplektatorius.common;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestFactory {
    private PageRequestFactory() {}

    public static Pageable normalize(Pageable pageable, Set<String> allowedSortFields) {
        if (pageable.getPageSize() < 1 || pageable.getPageSize() > 100 || pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Netinkamas puslapio dydis arba numeris.");
        }
        Sort sort = pageable.getSort().isUnsorted() ? Sort.by(Sort.Direction.DESC, "updatedAt") : pageable.getSort();
        for (Sort.Order order : sort) {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new IllegalArgumentException("Rūšiuoti pagal šį lauką negalima.");
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}


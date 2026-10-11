package lt.komplektatorius.offer;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import lt.komplektatorius.common.PageRequestFactory;
import lt.komplektatorius.common.error.NotFoundException;
import lt.komplektatorius.component.ComponentService;
import lt.komplektatorius.offer.dto.OfferRequest;
import lt.komplektatorius.offer.dto.OfferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferService {
    private static final Set<String> SORT_FIELDS = Set.of("supplierName", "priceEur", "stockStatus",
            "checkedAt", "createdAt", "updatedAt");
    private final OfferRepository offers;
    private final ComponentService componentService;

    public OfferService(OfferRepository offers, ComponentService componentService) {
        this.offers = offers;
        this.componentService = componentService;
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> list(UUID componentId, String supplier, StockStatus stockStatus,
                                    BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        componentService.require(componentId);
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimali kaina negali viršyti maksimalios.");
        }
        return offers.findAll(OfferSpecifications.filtered(componentId, supplier, stockStatus, minPrice, maxPrice),
                        PageRequestFactory.normalize(pageable, SORT_FIELDS)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OfferResponse get(UUID id) { return toResponse(require(id)); }

    @Transactional
    public OfferResponse create(UUID componentId, OfferRequest request) {
        Offer offer = new Offer();
        offer.setComponent(componentService.require(componentId));
        apply(offer, request);
        return toResponse(offers.save(offer));
    }

    @Transactional
    public OfferResponse update(UUID id, OfferRequest request) {
        Offer offer = require(id);
        apply(offer, request);
        return toResponse(offer);
    }

    @Transactional
    public void delete(UUID id) { offers.delete(require(id)); }

    Offer require(UUID id) {
        return offers.findById(id).orElseThrow(() -> new NotFoundException("Pasiūlymas nerastas."));
    }

    public OfferResponse toResponse(Offer o) {
        return new OfferResponse(o.getId(), o.getComponent().getId(), o.getSupplierName(), o.getPriceEur(),
                o.getProductUrl(), o.getStockStatus(), o.getCheckedAt(), o.getCreatedAt(), o.getUpdatedAt());
    }

    private void apply(Offer offer, OfferRequest request) {
        URI uri = URI.create(request.productUrl());
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("Pasiūlymo adresas turi būti pilnas HTTPS URL.");
        }
        offer.setSupplierName(request.supplierName().trim());
        offer.setPriceEur(request.priceEur());
        offer.setProductUrl(uri.toString());
        offer.setStockStatus(request.stockStatus());
        offer.setCheckedAt(request.checkedAt());
    }
}


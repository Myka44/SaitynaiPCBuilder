package lt.komplektatorius.offer;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfferRepository extends JpaRepository<Offer, UUID>, JpaSpecificationExecutor<Offer> {
    List<Offer> findByComponentIdAndStockStatusInOrderByPriceEurAsc(UUID componentId, List<StockStatus> statuses);
}


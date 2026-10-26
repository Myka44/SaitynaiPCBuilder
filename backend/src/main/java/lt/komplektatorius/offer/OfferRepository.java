package lt.komplektatorius.offer;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OfferRepository extends JpaRepository<Offer, UUID>, JpaSpecificationExecutor<Offer> {
    List<Offer> findByComponentIdAndStockStatusInOrderByPriceEurAsc(UUID componentId, List<StockStatus> statuses);

    List<Offer> findByComponentIdInOrderByPriceEurAsc(List<UUID> componentIds);

    @Query("select distinct o.supplierName from Offer o order by o.supplierName")
    List<String> findSupplierNames();
}


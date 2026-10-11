package lt.komplektatorius.offer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lt.komplektatorius.common.model.BaseEntity;
import lt.komplektatorius.component.Component;

@Entity
@Table(name = "offers", uniqueConstraints = @UniqueConstraint(
        name = "uk_offer_component_supplier", columnNames = {"component_id", "supplier_name"}))
public class Offer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "price_eur", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceEur;

    @Column(name = "product_url", nullable = false, length = 500)
    private String productUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 30)
    private StockStatus stockStatus;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getPriceEur() { return priceEur; }
    public void setPriceEur(BigDecimal priceEur) { this.priceEur = priceEur; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public StockStatus getStockStatus() { return stockStatus; }
    public void setStockStatus(StockStatus stockStatus) { this.stockStatus = stockStatus; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}


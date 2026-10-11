package lt.komplektatorius.component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lt.komplektatorius.common.model.BaseEntity;

@Entity
@Table(name = "components", uniqueConstraints = @UniqueConstraint(
        name = "uk_component_identity", columnNames = {"manufacturer", "model", "category"}))
public class Component extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComponentCategory category;

    @Column(nullable = false, length = 80)
    private String manufacturer;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "power_watts")
    private Integer powerWatts;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ComponentCategory getCategory() { return category; }
    public void setCategory(ComponentCategory category) { this.category = category; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPowerWatts() { return powerWatts; }
    public void setPowerWatts(Integer powerWatts) { this.powerWatts = powerWatts; }
}


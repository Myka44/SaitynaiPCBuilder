package lt.komplektatorius.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lt.komplektatorius.auth.User;
import lt.komplektatorius.common.model.BaseEntity;
import lt.komplektatorius.component.Component;

@Entity
@Table(name = "configurations")
public class BuildConfiguration extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "budget_eur", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetEur;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 30)
    private UsageType usageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @ManyToMany
    @JoinTable(name = "configuration_components",
            joinColumns = @JoinColumn(name = "configuration_id"),
            inverseJoinColumns = @JoinColumn(name = "component_id"))
    private Set<Component> components = new LinkedHashSet<>();

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBudgetEur() { return budgetEur; }
    public void setBudgetEur(BigDecimal budgetEur) { this.budgetEur = budgetEur; }
    public UsageType getUsageType() { return usageType; }
    public void setUsageType(UsageType usageType) { this.usageType = usageType; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public Set<Component> getComponents() { return components; }
}


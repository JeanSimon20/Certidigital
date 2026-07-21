package com.certidigital.platform.policy.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "policy_conditions",
    indexes = {
        @Index(name = "idx_policy_conditions_policy", columnList = "policy_id")
    }
)
public class PolicyConditionJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_policy_conditions_policy")
    )
    private IssuancePolicyJpaEntity policy;

    @Column(name = "condition_type", length = 100, nullable = false)
    private String conditionType;

    @Column(name = "threshold_value", precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "description", length = 500)
    private String description;

    public PolicyConditionJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public IssuancePolicyJpaEntity getPolicy() { return policy; }
    public void setPolicy(IssuancePolicyJpaEntity policy) { this.policy = policy; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

package com.certidigital.platform.policy.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "issuance_policies",
    indexes = {
        @Index(name = "idx_issuance_policies_tenant", columnList = "tenant_id"),
        @Index(name = "idx_issuance_policies_status", columnList = "tenant_id, status")
    }
)
public class IssuancePolicyJpaEntity extends AuditableEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logical_operator", length = 10, nullable = false)
    private String logicalOperator = "AND";

    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = false;

    @Column(name = "credential_validity_days")
    private Integer credentialValidityDays;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "DRAFT";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @OneToMany(
        mappedBy = "policy",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PolicyConditionJpaEntity> conditions = new ArrayList<>();

    public IssuancePolicyJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogicalOperator() { return logicalOperator; }
    public void setLogicalOperator(String logicalOperator) { this.logicalOperator = logicalOperator; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public Integer getCredentialValidityDays() { return credentialValidityDays; }
    public void setCredentialValidityDays(Integer credentialValidityDays) { this.credentialValidityDays = credentialValidityDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<PolicyConditionJpaEntity> getConditions() { return conditions; }
    public void setConditions(List<PolicyConditionJpaEntity> conditions) { this.conditions = conditions; }
}

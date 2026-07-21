package com.certidigital.platform.organization.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(
    name = "credential_templates",
    indexes = {
        @Index(name = "idx_cred_templates_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_cred_templates_status",    columnList = "tenant_id, status")
    }
)
public class CredentialTemplateJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "credential_type", length = 50, nullable = false)
    private String credentialType = "CERTIFICATE";

    @Column(name = "visual_layout", columnDefinition = "TEXT")
    private String visualLayout;

    @Column(name = "attribute_schema", columnDefinition = "TEXT")
    private String attributeSchema;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "DRAFT";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    public CredentialTemplateJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }

    public String getVisualLayout() { return visualLayout; }
    public void setVisualLayout(String visualLayout) { this.visualLayout = visualLayout; }

    public String getAttributeSchema() { return attributeSchema; }
    public void setAttributeSchema(String attributeSchema) { this.attributeSchema = attributeSchema; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

package com.certidigital.platform.iam.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "roles",
    indexes = {
        @Index(name = "idx_roles_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_roles_is_system", columnList = "is_system_role")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_roles_name_tenant",
            columnNames = {"name", "tenant_id"}
        )
    }
)
public class RoleJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36)
    private String tenantId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @OneToMany(
        mappedBy = "role",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<RolePermissionJpaEntity> permissions = new HashSet<>();

    public RoleJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsSystemRole() { return isSystemRole; }
    public void setIsSystemRole(Boolean isSystemRole) { this.isSystemRole = isSystemRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Set<RolePermissionJpaEntity> getPermissions() { return permissions; }
    public void setPermissions(Set<RolePermissionJpaEntity> permissions) { this.permissions = permissions; }
}

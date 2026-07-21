package com.certidigital.platform.iam.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(
    name = "role_permissions",
    indexes = {
        @Index(name = "idx_role_permissions_role_id", columnList = "role_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_role_permissions_role_resource_action",
            columnNames = {"role_id", "resource", "action"}
        )
    }
)
public class RolePermissionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "role_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_role_permissions_role")
    )
    private RoleJpaEntity role;

    @Column(name = "resource", length = 100, nullable = false)
    private String resource;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    public RolePermissionJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RoleJpaEntity getRole() { return role; }
    public void setRole(RoleJpaEntity role) { this.role = role; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String toPermissionString() {
        return resource + ":" + action;
    }
}

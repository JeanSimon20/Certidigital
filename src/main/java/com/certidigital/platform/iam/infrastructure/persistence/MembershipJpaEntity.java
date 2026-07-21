package com.certidigital.platform.iam.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "memberships",
    indexes = {
        @Index(name = "idx_memberships_user_id",   columnList = "user_id"),
        @Index(name = "idx_memberships_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_memberships_status",    columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_memberships_user_tenant",
            columnNames = {"user_id", "tenant_id"}
        )
    }
)
public class MembershipJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_memberships_user")
    )
    private UserJpaEntity user;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "membership_roles",
        joinColumns = @JoinColumn(
            name = "membership_id",
            foreignKey = @ForeignKey(name = "fk_membership_roles_membership")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "role_id",
            foreignKey = @ForeignKey(name = "fk_membership_roles_role")
        )
    )
    private Set<RoleJpaEntity> roles = new HashSet<>();

    public MembershipJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public UserJpaEntity getUser() { return user; }
    public void setUser(UserJpaEntity user) { this.user = user; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Set<RoleJpaEntity> getRoles() { return roles; }
    public void setRoles(Set<RoleJpaEntity> roles) { this.roles = roles; }
}

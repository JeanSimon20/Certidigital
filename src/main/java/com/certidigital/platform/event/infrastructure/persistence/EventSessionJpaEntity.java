package com.certidigital.platform.event.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "event_sessions",
    indexes = {
        @Index(name = "idx_event_sessions_event_id",  columnList = "event_id"),
        @Index(name = "idx_event_sessions_tenant_id", columnList = "tenant_id")
    }
)
public class EventSessionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "event_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_event_sessions_event")
    )
    private EventJpaEntity event;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "location_override", length = 500)
    private String locationOverride;

    @Column(name = "session_order", nullable = false)
    private Integer sessionOrder = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public EventSessionJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public EventJpaEntity getEvent() { return event; }
    public void setEvent(EventJpaEntity event) { this.event = event; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLocationOverride() { return locationOverride; }
    public void setLocationOverride(String locationOverride) { this.locationOverride = locationOverride; }

    public Integer getSessionOrder() { return sessionOrder; }
    public void setSessionOrder(Integer sessionOrder) { this.sessionOrder = sessionOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

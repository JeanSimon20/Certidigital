package com.certidigital.platform.event.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_events_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_events_status",    columnList = "tenant_id, status"),
        @Index(name = "idx_events_dates",     columnList = "start_date, end_date")
    }
)
public class EventJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType = "COURSE";

    @Column(name = "mode", length = 30, nullable = false)
    private String mode = "IN_PERSON";

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "timezone", length = 100, nullable = false)
    private String timezone = "UTC";

    @Column(name = "location_name", length = 500)
    private String locationName;

    @Column(name = "location_address", columnDefinition = "TEXT")
    private String locationAddress;

    @Column(name = "virtual_url", length = 1000)
    private String virtualUrl;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "issuance_policy_id", length = 36)
    private String issuancePolicyId;

    @Column(name = "credential_template_id", length = 36)
    private String credentialTemplateId;

    @Column(name = "organizer_user_id", length = 36)
    private String organizerUserId;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "DRAFT";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @OneToMany(
        mappedBy = "event",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("sessionOrder ASC")
    private List<EventSessionJpaEntity> sessions = new ArrayList<>();

    public EventJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getVirtualUrl() { return virtualUrl; }
    public void setVirtualUrl(String virtualUrl) { this.virtualUrl = virtualUrl; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getIssuancePolicyId() { return issuancePolicyId; }
    public void setIssuancePolicyId(String issuancePolicyId) { this.issuancePolicyId = issuancePolicyId; }

    public String getCredentialTemplateId() { return credentialTemplateId; }
    public void setCredentialTemplateId(String credentialTemplateId) { this.credentialTemplateId = credentialTemplateId; }

    public String getOrganizerUserId() { return organizerUserId; }
    public void setOrganizerUserId(String organizerUserId) { this.organizerUserId = organizerUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<EventSessionJpaEntity> getSessions() { return sessions; }
    public void setSessions(List<EventSessionJpaEntity> sessions) { this.sessions = sessions; }
}

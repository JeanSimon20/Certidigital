package com.certidigital.platform.event.application.dto;

import java.time.LocalDateTime;

public class EventResponse {

    private String id;
    private String tenantId;
    private String tenantName;
    private String name;
    private String description;
    private String eventType;
    private String mode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String timezone;
    private String locationName;
    private String locationAddress;
    private String virtualUrl;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private Double price;
    private Boolean isFree;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;

    public EventResponse() {}

    public EventResponse(
        String id, String tenantId, String tenantName, String name, String description,
        String eventType, String mode, LocalDateTime startDate, LocalDateTime endDate,
        String timezone, String locationName, String locationAddress, String virtualUrl,
        Integer maxCapacity, Integer enrolledCount, Double price, Boolean isFree,
        String status, String createdBy, LocalDateTime createdAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.name = name;
        this.description = description;
        this.eventType = eventType;
        this.mode = mode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timezone = timezone;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.virtualUrl = virtualUrl;
        this.maxCapacity = maxCapacity;
        this.enrolledCount = enrolledCount;
        this.price = price;
        this.isFree = isFree;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

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

    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Boolean getIsFree() { return isFree; }
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

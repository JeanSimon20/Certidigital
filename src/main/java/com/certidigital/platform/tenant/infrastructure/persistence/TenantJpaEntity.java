package com.certidigital.platform.tenant.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

/**
 * JPA Entity: tenants
 * Aggregate Root: Tenant [Platform Management BC]
 */
@Entity
@Table(
    name = "tenants",
    indexes = {
        @Index(name = "idx_tenants_status", columnList = "status")
    }
)
public class TenantJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "legal_name", length = 255, nullable = false)
    private String legalName;

    @Column(name = "commercial_name", length = 255)
    private String commercialName;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(name = "sector", length = 100, nullable = false)
    private String sector;

    @Column(name = "country_code", length = 3, nullable = false)
    private String countryCode;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "PENDING";

    @Column(name = "contact_name", length = 255, nullable = false)
    private String contactName;

    @Column(name = "contact_email", length = 255, nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "service_plan", length = 50, nullable = false)
    private String servicePlan = "STANDARD";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    public TenantJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getCommercialName() { return commercialName; }
    public void setCommercialName(String commercialName) { this.commercialName = commercialName; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getServicePlan() { return servicePlan; }
    public void setServicePlan(String servicePlan) { this.servicePlan = servicePlan; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

package com.certidigital.platform.tenant.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "tenant_registration_requests",
    indexes = {
        @Index(name = "idx_tenant_reg_requests_status", columnList = "status"),
        @Index(name = "idx_tenant_reg_requests_email",  columnList = "applicant_email")
    }
)
public class TenantRegistrationRequestJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "applicant_name", length = 255, nullable = false)
    private String applicantName;

    @Column(name = "applicant_email", length = 255, nullable = false)
    private String applicantEmail;

    @Column(name = "applicant_phone", length = 50)
    private String applicantPhone;

    @Column(name = "org_legal_name", length = 255, nullable = false)
    private String orgLegalName;

    @Column(name = "org_commercial_name", length = 255)
    private String orgCommercialName;

    @Column(name = "org_tax_id", length = 100)
    private String orgTaxId;

    @Column(name = "org_sector", length = 100, nullable = false)
    private String orgSector;

    @Column(name = "org_country_code", length = 3, nullable = false)
    private String orgCountryCode;

    @Column(name = "org_website", length = 500)
    private String orgWebsite;

    @Column(name = "use_case_description", columnDefinition = "TEXT")
    private String useCaseDescription;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "SUBMITTED";

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "tenant_id", length = 36)
    private String tenantId;

    public TenantRegistrationRequestJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }

    public String getOrgLegalName() { return orgLegalName; }
    public void setOrgLegalName(String orgLegalName) { this.orgLegalName = orgLegalName; }

    public String getOrgCommercialName() { return orgCommercialName; }
    public void setOrgCommercialName(String orgCommercialName) { this.orgCommercialName = orgCommercialName; }

    public String getOrgTaxId() { return orgTaxId; }
    public void setOrgTaxId(String orgTaxId) { this.orgTaxId = orgTaxId; }

    public String getOrgSector() { return orgSector; }
    public void setOrgSector(String orgSector) { this.orgSector = orgSector; }

    public String getOrgCountryCode() { return orgCountryCode; }
    public void setOrgCountryCode(String orgCountryCode) { this.orgCountryCode = orgCountryCode; }

    public String getOrgWebsite() { return orgWebsite; }
    public void setOrgWebsite(String orgWebsite) { this.orgWebsite = orgWebsite; }

    public String getUseCaseDescription() { return useCaseDescription; }
    public void setUseCaseDescription(String useCaseDescription) { this.useCaseDescription = useCaseDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}

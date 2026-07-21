package com.certidigital.platform.iam.application.dto;

public class TenantSummaryResponse {

    private String tenantId;
    private String legalName;
    private String commercialName;
    private String membershipStatus;
    private String servicePlan;

    public TenantSummaryResponse() {}

    public TenantSummaryResponse(String tenantId, String legalName, String commercialName, String membershipStatus, String servicePlan) {
        this.tenantId = tenantId;
        this.legalName = legalName;
        this.commercialName = commercialName;
        this.membershipStatus = membershipStatus;
        this.servicePlan = servicePlan;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getCommercialName() { return commercialName; }
    public void setCommercialName(String commercialName) { this.commercialName = commercialName; }

    public String getMembershipStatus() { return membershipStatus; }
    public void setMembershipStatus(String membershipStatus) { this.membershipStatus = membershipStatus; }

    public String getServicePlan() { return servicePlan; }
    public void setServicePlan(String servicePlan) { this.servicePlan = servicePlan; }
}

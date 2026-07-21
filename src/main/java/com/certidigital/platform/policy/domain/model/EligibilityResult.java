package com.certidigital.platform.policy.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EligibilityResult {

    private String enrollmentId;
    private String policyId;
    private String status; // ELIGIBLE, NOT_ELIGIBLE
    private String summaryReason;
    private List<RuleEvaluationDetail> ruleResults = new ArrayList<>();
    private LocalDateTime evaluatedAt;

    public EligibilityResult() {
        this.evaluatedAt = LocalDateTime.now();
    }

    public EligibilityResult(String enrollmentId, String policyId, String status, String summaryReason, List<RuleEvaluationDetail> ruleResults) {
        this.enrollmentId = enrollmentId;
        this.policyId = policyId;
        this.status = status;
        this.summaryReason = summaryReason;
        this.ruleResults = ruleResults != null ? ruleResults : new ArrayList<>();
        this.evaluatedAt = LocalDateTime.now();
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummaryReason() { return summaryReason; }
    public void setSummaryReason(String summaryReason) { this.summaryReason = summaryReason; }

    public List<RuleEvaluationDetail> getRuleResults() { return ruleResults; }
    public void setRuleResults(List<RuleEvaluationDetail> ruleResults) { this.ruleResults = ruleResults; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}

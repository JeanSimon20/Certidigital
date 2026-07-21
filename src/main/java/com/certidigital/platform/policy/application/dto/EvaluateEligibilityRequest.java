package com.certidigital.platform.policy.application.dto;

import jakarta.validation.constraints.NotBlank;

public class EvaluateEligibilityRequest {

    @NotBlank(message = "El ID de inscripción es obligatorio")
    private String enrollmentId;

    private String policyId;

    private Double evaluationScore;

    public EvaluateEligibilityRequest() {}

    public EvaluateEligibilityRequest(String enrollmentId, String policyId, Double evaluationScore) {
        this.enrollmentId = enrollmentId;
        this.policyId = policyId;
        this.evaluationScore = evaluationScore;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public Double getEvaluationScore() { return evaluationScore; }
    public void setEvaluationScore(Double evaluationScore) { this.evaluationScore = evaluationScore; }
}

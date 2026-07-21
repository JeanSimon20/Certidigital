package com.certidigital.platform.policy.domain.model;

import java.time.LocalDateTime;

public class RuleEvaluationDetail {

    private String ruleType;
    private String ruleName;
    private boolean passed;
    private String expectedValue;
    private String obtainedValue;
    private String reason;
    private LocalDateTime evaluatedAt;

    public RuleEvaluationDetail() {
        this.evaluatedAt = LocalDateTime.now();
    }

    public RuleEvaluationDetail(String ruleType, String ruleName, boolean passed, String expectedValue, String obtainedValue, String reason) {
        this.ruleType = ruleType;
        this.ruleName = ruleName;
        this.passed = passed;
        this.expectedValue = expectedValue;
        this.obtainedValue = obtainedValue;
        this.reason = reason;
        this.evaluatedAt = LocalDateTime.now();
    }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getObtainedValue() { return obtainedValue; }
    public void setObtainedValue(String obtainedValue) { this.obtainedValue = obtainedValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}

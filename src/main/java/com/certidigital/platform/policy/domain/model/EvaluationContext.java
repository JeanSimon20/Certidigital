package com.certidigital.platform.policy.domain.model;

import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;

import java.util.HashMap;
import java.util.Map;

public class EvaluationContext {

    private final EnrollmentJpaEntity enrollment;
    private final EventJpaEntity event;
    private final Double evaluationScore;
    private final Map<String, Object> attributes = new HashMap<>();

    public EvaluationContext(EnrollmentJpaEntity enrollment, EventJpaEntity event, Double evaluationScore) {
        this.enrollment = enrollment;
        this.event = event;
        this.evaluationScore = evaluationScore;
    }

    public EnrollmentJpaEntity getEnrollment() { return enrollment; }
    public EventJpaEntity getEvent() { return event; }
    public Double getEvaluationScore() { return evaluationScore; }
    public Map<String, Object> getAttributes() { return attributes; }
}

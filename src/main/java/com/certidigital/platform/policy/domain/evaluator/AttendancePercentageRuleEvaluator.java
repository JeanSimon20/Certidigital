package com.certidigital.platform.policy.domain.evaluator;

import com.certidigital.platform.policy.domain.model.EligibilityRuleType;
import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.PolicyConditionJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AttendancePercentageRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(String conditionType) {
        if (conditionType == null) return false;
        String type = conditionType.toUpperCase();
        return type.contains("ATTENDANCE") || type.contains("ASISTENCIA");
    }

    @Override
    public RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context) {
        BigDecimal threshold = condition.getThresholdValue() != null ? condition.getThresholdValue() : BigDecimal.valueOf(80.0);
        BigDecimal obtainedPct = context.getEnrollment().getAttendancePercentage();
        double obtainedValue = obtainedPct != null ? obtainedPct.doubleValue() : 0.0;

        boolean passed = obtainedValue >= threshold.doubleValue();

        String expected = ">= " + threshold + "%";
        String obtained = String.format("%.2f%%", obtainedValue);
        String reason = passed
                ? "El porcentaje de asistencia (" + obtained + ") cumple con el mínimo requerido (" + expected + ")."
                : "Porcentaje de asistencia insuficiente. Obtenido: " + obtained + ", mínimo requerido: " + expected;

        return new RuleEvaluationDetail(
                EligibilityRuleType.ATTENDANCE_PERCENTAGE.name(),
                condition.getDescription() != null ? condition.getDescription() : "Porcentaje Mínimo de Asistencia",
                passed,
                expected,
                obtained,
                reason
        );
    }
}

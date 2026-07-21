package com.certidigital.platform.policy.domain.evaluator;

import com.certidigital.platform.policy.domain.model.EligibilityRuleType;
import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.PolicyConditionJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EvaluationScoreRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return EligibilityRuleType.EVALUATION_SCORE.name().equalsIgnoreCase(conditionType);
    }

    @Override
    public RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context) {
        BigDecimal threshold = condition.getThresholdValue() != null ? condition.getThresholdValue() : BigDecimal.valueOf(14.0);
        Double score = context.getEvaluationScore();

        boolean passed = score != null && BigDecimal.valueOf(score).compareTo(threshold) >= 0;

        String expected = ">= " + threshold;
        String obtained = score != null ? String.format("%.2f", score) : "SIN_EVALUACION";
        String reason = passed
                ? "La calificación obtenida (" + obtained + ") cumple el puntaje mínimo de aprobación (" + expected + ")."
                : "Calificación académica insuficiente. Obtenida: " + obtained + ", mínimo requerido: " + expected;

        return new RuleEvaluationDetail(
                EligibilityRuleType.EVALUATION_SCORE.name(),
                condition.getDescription() != null ? condition.getDescription() : "Puntaje Mínimo de Evaluación",
                passed,
                expected,
                obtained,
                reason
        );
    }
}

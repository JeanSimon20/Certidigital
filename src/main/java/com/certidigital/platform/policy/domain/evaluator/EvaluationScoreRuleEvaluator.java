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
        if (conditionType == null) return false;
        String type = conditionType.toUpperCase();
        return type.contains("EVALUATION") || type.contains("SCORE") || type.contains("NOTA") || type.contains("GRADE") || type.contains("CALIFICACION");
    }

    @Override
    public RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context) {
        BigDecimal threshold = condition.getThresholdValue() != null ? condition.getThresholdValue() : BigDecimal.valueOf(14.0);
        Double score = context.getEvaluationScore();

        double effectiveScore = score != null ? score : 0.0;
        if (score != null && score <= 20.0 && threshold.doubleValue() > 20.0) {
            // Normalizar escala de 20 puntos a escala de 100 puntos (ej. 16.0/20 -> 80.0/100)
            effectiveScore = score * 5.0;
        }

        boolean passed = score != null && BigDecimal.valueOf(effectiveScore).compareTo(threshold) >= 0;

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

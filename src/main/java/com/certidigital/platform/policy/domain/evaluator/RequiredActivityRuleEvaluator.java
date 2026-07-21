package com.certidigital.platform.policy.domain.evaluator;

import com.certidigital.platform.policy.domain.model.EligibilityRuleType;
import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.PolicyConditionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RequiredActivityRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return EligibilityRuleType.REQUIRED_ACTIVITY.name().equalsIgnoreCase(conditionType);
    }

    @Override
    public RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context) {
        Object activityFlag = context.getAttributes().get("REQUIRED_ACTIVITY_COMPLETED");
        boolean passed = Boolean.TRUE.equals(activityFlag);

        String expected = "COMPLETED";
        String obtained = passed ? "COMPLETED" : "PENDING";
        String reason = passed
                ? "Las actividades obligatorias del programa fueron completadas satisfactoriamente."
                : "No se han completado las actividades obligatorias requeridas por el plan académico.";

        return new RuleEvaluationDetail(
                EligibilityRuleType.REQUIRED_ACTIVITY.name(),
                condition.getDescription() != null ? condition.getDescription() : "Actividades Obligatorias Completadas",
                passed,
                expected,
                obtained,
                reason
        );
    }
}

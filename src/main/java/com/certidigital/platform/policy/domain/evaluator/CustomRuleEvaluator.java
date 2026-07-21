package com.certidigital.platform.policy.domain.evaluator;

import com.certidigital.platform.policy.domain.model.EligibilityRuleType;
import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.PolicyConditionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return EligibilityRuleType.CUSTOM_RULE.name().equalsIgnoreCase(conditionType);
    }

    @Override
    public RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context) {
        Object customResult = context.getAttributes().get(condition.getConditionType());
        boolean passed = Boolean.TRUE.equals(customResult);

        return new RuleEvaluationDetail(
                EligibilityRuleType.CUSTOM_RULE.name(),
                condition.getDescription() != null ? condition.getDescription() : "Regla Personalizada",
                passed,
                "TRUE",
                passed ? "TRUE" : "FALSE",
                passed ? "Regla personalizada validada con éxito." : "Regla personalizada no fue satisfecha."
        );
    }
}

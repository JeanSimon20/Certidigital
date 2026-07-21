package com.certidigital.platform.policy.domain.evaluator;

import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.PolicyConditionJpaEntity;

public interface RuleEvaluator {

    boolean supports(String conditionType);

    RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context);
}

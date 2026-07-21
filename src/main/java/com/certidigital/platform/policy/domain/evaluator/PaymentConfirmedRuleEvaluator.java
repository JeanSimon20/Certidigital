package com.certidigital.platform.policy.domain.evaluator;

import com.certidigital.platform.policy.domain.model.EligibilityRuleType;
import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.PolicyConditionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmedRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(String conditionType) {
        return EligibilityRuleType.PAYMENT_CONFIRMED.name().equalsIgnoreCase(conditionType);
    }

    @Override
    public RuleEvaluationDetail evaluate(PolicyConditionJpaEntity condition, EvaluationContext context) {
        String paymentStatus = context.getEnrollment().getPaymentStatus();
        if (paymentStatus == null) {
            paymentStatus = "PENDING_PAYMENT";
        }

        boolean passed = "COMPLETED".equalsIgnoreCase(paymentStatus)
                || "CONFIRMED".equalsIgnoreCase(paymentStatus)
                || "NOT_REQUIRED".equalsIgnoreCase(paymentStatus);

        String expected = "CONFIRMED / COMPLETED";
        String obtained = paymentStatus;
        String reason = passed
                ? "El pago fue confirmado correctamente o el evento es gratuito."
                : "El pago de la inscripción no ha sido confirmado. Estado actual: " + paymentStatus;

        return new RuleEvaluationDetail(
                EligibilityRuleType.PAYMENT_CONFIRMED.name(),
                condition.getDescription() != null ? condition.getDescription() : "Confirmación de Pago",
                passed,
                expected,
                obtained,
                reason
        );
    }
}

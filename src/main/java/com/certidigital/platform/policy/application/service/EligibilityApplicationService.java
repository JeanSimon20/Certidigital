package com.certidigital.platform.policy.application.service;

import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.policy.application.dto.EvaluateEligibilityRequest;
import com.certidigital.platform.policy.domain.evaluator.RuleEvaluator;
import com.certidigital.platform.policy.domain.model.EligibilityResult;
import com.certidigital.platform.policy.domain.model.EligibilityRuleType;
import com.certidigital.platform.policy.domain.model.EvaluationContext;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.policy.infrastructure.persistence.*;
import com.certidigital.platform.shared.infrastructure.security.TenantAccessDeniedException;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EligibilityApplicationService {

    private final IssuancePolicyJpaRepository policyRepository;
    private final EligibilityEvaluationJpaRepository evaluationRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final List<RuleEvaluator> evaluators;
    private final ObjectMapper objectMapper;

    public EligibilityApplicationService(
            IssuancePolicyJpaRepository policyRepository,
            EligibilityEvaluationJpaRepository evaluationRepository,
            EnrollmentJpaRepository enrollmentRepository,
            EventJpaRepository eventRepository,
            List<RuleEvaluator> evaluators,
            ObjectMapper objectMapper
    ) {
        this.policyRepository = policyRepository;
        this.evaluationRepository = evaluationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.evaluators = evaluators;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EligibilityResult evaluateEligibility(EvaluateEligibilityRequest request) {
        String activeTenantId = TenantContextHolder.requireTenantId();

        // 1. Consultar inscripción
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + request.getEnrollmentId()));

        // Validar aislamiento Multi-Tenant
        if (!activeTenantId.equals(enrollment.getTenantId())) {
            throw new TenantAccessDeniedException("La inscripción solicitada pertenece a otro Tenant.");
        }

        // 2. Consultar evento
        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado para la inscripción."));

        if (!activeTenantId.equals(event.getTenantId())) {
            throw new TenantAccessDeniedException("El evento de la inscripción pertenece a otro Tenant.");
        }

        // 3. Validar estado del evento (debe estar PUBLISHED o COMPLETED, no DRAFT)
        if ("DRAFT".equalsIgnoreCase(event.getStatus())) {
            RuleEvaluationDetail draftRuleDetail = new RuleEvaluationDetail(
                    "EVENT_STATUS",
                    "Estado del Evento",
                    false,
                    "PUBLISHED / COMPLETED",
                    "DRAFT",
                    "El evento aún se encuentra en estado borrador (DRAFT) y no ha finalizado."
            );

            EligibilityResult result = new EligibilityResult(
                    enrollment.getId(),
                    request.getPolicyId() != null ? request.getPolicyId() : "DEFAULT_POLICY",
                    "NOT_ELIGIBLE",
                    "Evento incompleto o en estado DRAFT",
                    List.of(draftRuleDetail)
            );

            saveEvaluationTrace(activeTenantId, result, enrollment, event);
            return result;
        }

        // 4. Resolver Política de Emisión
        IssuancePolicyJpaEntity policy = resolvePolicy(request.getPolicyId(), activeTenantId);

        // 5. Crear contexto de evaluación
        EvaluationContext context = new EvaluationContext(enrollment, event, request.getEvaluationScore());

        // 6. Evaluar Reglas
        List<RuleEvaluationDetail> ruleDetails = new ArrayList<>();
        boolean overallPassed = "AND".equalsIgnoreCase(policy.getLogicalOperator());

        for (PolicyConditionJpaEntity condition : policy.getConditions()) {
            RuleEvaluator evaluator = findEvaluator(condition.getConditionType());
            RuleEvaluationDetail detail;
            if (evaluator != null) {
                detail = evaluator.evaluate(condition, context);
            } else {
                detail = new RuleEvaluationDetail(
                        condition.getConditionType(),
                        condition.getDescription() != null ? condition.getDescription() : condition.getConditionType(),
                        false,
                        "SUPPORTED_RULE",
                        "UNSUPPORTED",
                        "No se encontró un evaluador de regla para el tipo: " + condition.getConditionType()
                );
            }
            ruleDetails.add(detail);

            if ("AND".equalsIgnoreCase(policy.getLogicalOperator())) {
                if (!detail.isPassed()) {
                    overallPassed = false;
                }
            } else if ("OR".equalsIgnoreCase(policy.getLogicalOperator())) {
                if (detail.isPassed()) {
                    overallPassed = true;
                }
            }
        }

        String finalStatus = overallPassed ? "ELIGIBLE" : "NOT_ELIGIBLE";
        String summaryReason = overallPassed
                ? "El participante cumple con todos los requisitos de elegibilidad configurados en la política."
                : "El participante no satisface uno o más criterios de elegibilidad requeridos.";

        EligibilityResult result = new EligibilityResult(
                enrollment.getId(),
                policy.getId(),
                finalStatus,
                summaryReason,
                ruleDetails
        );

        // 7. Guardar trazabilidad completa en Base de Datos
        saveEvaluationTrace(activeTenantId, result, enrollment, event);

        return result;
    }

    private IssuancePolicyJpaEntity resolvePolicy(String policyId, String tenantId) {
        if (policyId != null && !policyId.isBlank()) {
            return policyRepository.findByIdAndTenantId(policyId, tenantId)
                    .orElseGet(() -> createDefaultPolicy(tenantId));
        }

        List<IssuancePolicyJpaEntity> tenantPolicies = policyRepository.findAllByTenantId(tenantId);
        return tenantPolicies.stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .findFirst()
                .orElseGet(() -> createDefaultPolicy(tenantId));
    }

    private IssuancePolicyJpaEntity createDefaultPolicy(String tenantId) {
        IssuancePolicyJpaEntity defaultPolicy = new IssuancePolicyJpaEntity();
        defaultPolicy.setId("policy-default-" + tenantId.substring(0, Math.min(8, tenantId.length())));
        defaultPolicy.setTenantId(tenantId);
        defaultPolicy.setName("Política General de Elegibilidad Académica");
        defaultPolicy.setDescription("Política estándar: Pago Confirmado + Asistencia >= 80% + Evaluación >= 14.0");
        defaultPolicy.setLogicalOperator("AND");
        defaultPolicy.setStatus("ACTIVE");

        PolicyConditionJpaEntity cond1 = new PolicyConditionJpaEntity();
        cond1.setId(UUID.randomUUID().toString());
        cond1.setPolicy(defaultPolicy);
        cond1.setConditionType(EligibilityRuleType.PAYMENT_CONFIRMED.name());
        cond1.setDescription("Pago de inscripción confirmado");

        PolicyConditionJpaEntity cond2 = new PolicyConditionJpaEntity();
        cond2.setId(UUID.randomUUID().toString());
        cond2.setPolicy(defaultPolicy);
        cond2.setConditionType(EligibilityRuleType.ATTENDANCE_PERCENTAGE.name());
        cond2.setThresholdValue(BigDecimal.valueOf(80.0));
        cond2.setDescription("Mínimo 80% de asistencia requerida");

        PolicyConditionJpaEntity cond3 = new PolicyConditionJpaEntity();
        cond3.setId(UUID.randomUUID().toString());
        cond3.setPolicy(defaultPolicy);
        cond3.setConditionType(EligibilityRuleType.EVALUATION_SCORE.name());
        cond3.setThresholdValue(BigDecimal.valueOf(14.0));
        cond3.setDescription("Calificación mínima de 14.00 / 20.00");

        defaultPolicy.setConditions(List.of(cond1, cond2, cond3));
        return policyRepository.save(defaultPolicy);
    }

    private RuleEvaluator findEvaluator(String conditionType) {
        return evaluators.stream()
                .filter(e -> e.supports(conditionType))
                .findFirst()
                .orElse(null);
    }

    private void saveEvaluationTrace(String tenantId, EligibilityResult result, EnrollmentJpaEntity enrollment, EventJpaEntity event) {
        try {
            EligibilityEvaluationJpaEntity entity = new EligibilityEvaluationJpaEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setTenantId(tenantId);
            entity.setEnrollmentId(enrollment.getId());
            entity.setPolicyId(result.getPolicyId());
            entity.setResult(result.getStatus());

            String conditionResultsJson = objectMapper.writeValueAsString(result.getRuleResults());
            entity.setConditionResults(conditionResultsJson);

            Map<String, Object> evidenceMap = Map.of(
                    "eventName", event.getName(),
                    "eventStatus", event.getStatus(),
                    "participantName", enrollment.getParticipant() != null ? enrollment.getParticipant().getFullName() : "N/A",
                    "participantEmail", enrollment.getParticipant() != null ? enrollment.getParticipant().getEmail() : "N/A",
                    "enrollmentStatus", enrollment.getStatus(),
                    "paymentStatus", enrollment.getPaymentStatus() != null ? enrollment.getPaymentStatus() : "NONE",
                    "attendancePercentage", enrollment.getAttendancePercentage() != null ? enrollment.getAttendancePercentage().doubleValue() : 0.0,
                    "evaluatedAt", LocalDateTime.now().toString()
            );

            entity.setEvidenceSnapshot(objectMapper.writeValueAsString(evidenceMap));
            evaluationRepository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando trazabilidad de elegibilidad", e);
        }
    }

    @Transactional(readOnly = true)
    public List<EligibilityEvaluationJpaEntity> getEvaluationsForEnrollment(String enrollmentId) {
        String activeTenantId = TenantContextHolder.requireTenantId();
        return evaluationRepository.findAllByTenantIdAndEnrollmentId(activeTenantId, enrollmentId);
    }
}

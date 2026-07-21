package com.certidigital.platform.policy.infrastructure.web;

import com.certidigital.platform.policy.application.dto.EvaluateEligibilityRequest;
import com.certidigital.platform.policy.application.service.EligibilityApplicationService;
import com.certidigital.platform.policy.domain.model.EligibilityResult;
import com.certidigital.platform.policy.infrastructure.persistence.EligibilityEvaluationJpaEntity;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityController {

    private final EligibilityApplicationService eligibilityService;

    public EligibilityController(EligibilityApplicationService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EligibilityResult>> evaluateEligibility(
            @Valid @RequestBody EvaluateEligibilityRequest request
    ) {
        EligibilityResult result = eligibilityService.evaluateEligibility(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Evaluación de elegibilidad procesada exitosamente"));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'PARTICIPANT', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EligibilityEvaluationJpaEntity>>> getEnrollmentEvaluations(
            @PathVariable String enrollmentId
    ) {
        List<EligibilityEvaluationJpaEntity> evaluations = eligibilityService.getEvaluationsForEnrollment(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(evaluations, "Evaluaciones de elegibilidad recuperadas"));
    }
}

package com.certidigital.platform.participation.presentation;

import com.certidigital.platform.participation.application.dto.EvaluationResultResponse;
import com.certidigital.platform.participation.application.dto.RecordEvaluationRequest;
import com.certidigital.platform.participation.application.service.EvaluationApplicationService;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationApplicationService evaluationService;

    public EvaluationController(EvaluationApplicationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'TEACHER', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<EvaluationResultResponse>> recordEvaluation(
        @RequestBody RecordEvaluationRequest request,
        Authentication authentication
    ) {
        String tenantId = TenantContextHolder.getTenantId();
        String recordedBy = authentication != null ? authentication.getName() : "user-teacher-0002-aaaa-bbbb-cccccccc";
        EvaluationResultResponse response = evaluationService.recordEvaluation(request, tenantId, recordedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Calificación registrada correctamente"));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<ApiResponse<List<EvaluationResultResponse>>> getEnrollmentEvaluations(
        @PathVariable String enrollmentId
    ) {
        String tenantId = TenantContextHolder.getTenantId();
        List<EvaluationResultResponse> list = evaluationService.getEnrollmentEvaluations(enrollmentId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(list, "Evaluaciones obtenidas"));
    }
}

package com.certidigital.platform.participation.presentation;

import com.certidigital.platform.participation.application.dto.CreateEnrollmentRequest;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.application.service.EnrollmentApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentApplicationService enrollmentService;

    public EnrollmentController(EnrollmentApplicationService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollParticipant(
        @RequestBody CreateEnrollmentRequest request,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        EnrollmentResponse response = enrollmentService.enrollParticipant(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Inscripción realizada exitosamente"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(Authentication authentication) {
        String userId = authentication.getName();
        List<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(userId);
        return ResponseEntity.ok(ApiResponse.success(enrollments, "Mis inscripciones cargadas exitosamente"));
    }
}

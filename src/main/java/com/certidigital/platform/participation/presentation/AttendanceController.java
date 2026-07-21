package com.certidigital.platform.participation.presentation;

import com.certidigital.platform.participation.application.dto.AttendanceRecordResponse;
import com.certidigital.platform.participation.application.dto.RecordAttendanceRequest;
import com.certidigital.platform.participation.application.service.AttendanceApplicationService;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceApplicationService attendanceService;

    public AttendanceController(AttendanceApplicationService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> recordAttendance(
        @RequestBody RecordAttendanceRequest request,
        Authentication authentication
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        String userId = authentication.getName();
        AttendanceRecordResponse response = attendanceService.recordAttendance(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Asistencia registrada exitosamente"));
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getSessionAttendance(
        @PathVariable String sessionId
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        List<AttendanceRecordResponse> records = attendanceService.getSessionAttendance(sessionId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(records, "Registros de asistencia obtenidos"));
    }
}

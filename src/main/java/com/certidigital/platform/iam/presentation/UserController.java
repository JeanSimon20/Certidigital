package com.certidigital.platform.iam.presentation;

import com.certidigital.platform.iam.application.dto.TenantSummaryResponse;
import com.certidigital.platform.iam.application.dto.UserProfileResponse;
import com.certidigital.platform.iam.application.service.UserApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userService;

    public UserController(UserApplicationService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        UserProfileResponse profile = userService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Perfil de usuario obtenido correctamente."));
    }

    @GetMapping("/me/tenants")
    public ResponseEntity<ApiResponse<List<TenantSummaryResponse>>> getCurrentUserTenants(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        List<TenantSummaryResponse> tenants = userService.getUserTenants(userId);
        return ResponseEntity.ok(ApiResponse.success(tenants, "Tenants del usuario obtenidos correctamente."));
    }
}

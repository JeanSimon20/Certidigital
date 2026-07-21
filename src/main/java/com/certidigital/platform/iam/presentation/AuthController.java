package com.certidigital.platform.iam.presentation;

import com.certidigital.platform.iam.application.dto.*;
import com.certidigital.platform.iam.application.service.AuthApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse userProfile = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(userProfile, "Usuario registrado exitosamente en CertiDigital."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String ipAddress = extractIpAddress(httpRequest);
        AuthResponse authResponse = authService.login(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Autenticación exitosa."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request,
        HttpServletRequest httpRequest
    ) {
        String ipAddress = extractIpAddress(httpRequest);
        AuthResponse authResponse = authService.refreshToken(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token renovado exitosamente."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            String userId = authentication.getPrincipal().toString();
            authService.logout(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Sesión cerrada exitosamente."));
    }

    @PostMapping("/switch-tenant")
    public ResponseEntity<ApiResponse<AuthResponse>> switchTenant(
        @Valid @RequestBody SwitchTenantRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Debe estar autenticado para cambiar de Tenant."));
        }
        String userId = authentication.getPrincipal().toString();
        String ipAddress = extractIpAddress(httpRequest);

        AuthResponse authResponse = authService.switchTenant(userId, request.getTenantId(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Contexto de Tenant cambiado exitosamente."));
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

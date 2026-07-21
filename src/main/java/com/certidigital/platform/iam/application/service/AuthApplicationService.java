package com.certidigital.platform.iam.application.service;

import com.certidigital.platform.audit.application.service.SecurityAuditService;
import com.certidigital.platform.iam.application.dto.*;
import com.certidigital.platform.iam.infrastructure.persistence.*;
import com.certidigital.platform.shared.infrastructure.security.JwtProvider;
import com.certidigital.platform.shared.infrastructure.security.TenantAccessDeniedException;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaEntity;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthApplicationService {

    private final UserJpaRepository userRepository;
    private final MembershipJpaRepository membershipRepository;
    private final RoleJpaRepository roleRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final TenantJpaRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final SecurityAuditService auditService;

    public AuthApplicationService(
        UserJpaRepository userRepository,
        MembershipJpaRepository membershipRepository,
        RoleJpaRepository roleRepository,
        RefreshTokenJpaRepository refreshTokenRepository,
        TenantJpaRepository tenantRepository,
        PasswordEncoder passwordEncoder,
        JwtProvider jwtProvider,
        SecurityAuditService auditService
    ) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.auditService = auditService;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email '" + request.getEmail() + "' ya se encuentra registrado en el sistema.");
        }

        UserJpaEntity user = new UserJpaEntity();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setEmailVerified(false);

        UserJpaEntity savedUser = userRepository.save(user);

        // Registro de auditoría
        auditService.logSecurityEvent(
            "USER_REGISTERED",
            savedUser.getId(),
            "USER",
            savedUser.getFullName(),
            null,
            "USER",
            savedUser.getId(),
            "SUCCESS",
            null,
            "{\"email\":\"" + savedUser.getEmail() + "\"}"
        );

        return new UserProfileResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getStatus(),
            savedUser.getEmailVerified(),
            savedUser.getLastLoginAt(),
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        UserJpaEntity user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
            .orElseThrow(() -> {
                auditService.logSecurityEvent(
                    "USER_LOGIN_FAILED",
                    null,
                    "USER",
                    request.getEmail(),
                    null,
                    "USER",
                    null,
                    "FAILURE",
                    ipAddress,
                    "{\"reason\":\"Usuario no encontrado\"}"
                );
                return new BadCredentialsException("Credenciales inválidas");
            });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditService.logSecurityEvent(
                "USER_LOGIN_FAILED",
                user.getId(),
                "USER",
                user.getFullName(),
                null,
                "USER",
                user.getId(),
                "FAILURE",
                ipAddress,
                "{\"reason\":\"Contraseña incorrecta\"}"
            );
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            auditService.logSecurityEvent(
                "USER_LOGIN_FAILED",
                user.getId(),
                "USER",
                user.getFullName(),
                null,
                "USER",
                user.getId(),
                "FAILURE",
                ipAddress,
                "{\"reason\":\"Usuario no activo\"}"
            );
            throw new TenantAccessDeniedException("La cuenta de usuario está desactivada o suspendida.");
        }

        // Actualizar fecha de último login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Cargar membresías activas del usuario
        List<MembershipJpaEntity> activeMemberships = membershipRepository.findByUserIdAndStatus(user.getId(), "ACTIVE");
        List<TenantSummaryResponse> availableTenants = buildAvailableTenants(activeMemberships);

        // Seleccionar Tenant activo por defecto si pertenece a exactamente 1 tenant
        String defaultTenantId = null;
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        if (activeMemberships.size() == 1) {
            MembershipJpaEntity m = activeMemberships.get(0);
            defaultTenantId = m.getTenantId();
            extractRolesAndPermissions(user.getId(), defaultTenantId, roles, permissions);
        }

        // Generar JWT y Refresh Token
        String accessToken = jwtProvider.generateAccessToken(
            user.getId(), user.getEmail(), user.getFullName(), defaultTenantId, roles, permissions
        );

        String refreshTokenValue = generateAndSaveRefreshToken(user, ipAddress);

        auditService.logSecurityEvent(
            "USER_LOGIN_SUCCESS",
            user.getId(),
            "USER",
            user.getFullName(),
            defaultTenantId,
            "USER",
            user.getId(),
            "SUCCESS",
            ipAddress,
            "{\"activeTenant\":\"" + defaultTenantId + "\"}"
        );

        return new AuthResponse(
            accessToken,
            refreshTokenValue,
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            defaultTenantId,
            roles,
            permissions,
            availableTenants
        );
    }

    @Transactional
    public AuthResponse switchTenant(String userId, String tenantId, String ipAddress) {
        UserJpaEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar que la organización exista y esté activa
        TenantJpaEntity tenant = tenantRepository.findByIdAndStatus(tenantId, "ACTIVE")
            .orElseThrow(() -> {
                auditService.logSecurityEvent(
                    "TENANT_ACCESS_DENIED",
                    userId,
                    "USER",
                    user.getFullName(),
                    tenantId,
                    "TENANT",
                    tenantId,
                    "FAILURE",
                    ipAddress,
                    "{\"reason\":\"Tenant no existe o no está activo\"}"
                );
                return new TenantAccessDeniedException("El Tenant seleccionado no existe o no se encuentra activo.");
            });

        // Verificar que la membresía del usuario en el Tenant exista y esté activa
        MembershipJpaEntity membership = membershipRepository
            .findActiveMembershipWithRolesAndPermissions(userId, tenantId)
            .orElseThrow(() -> {
                auditService.logSecurityEvent(
                    "TENANT_ACCESS_DENIED",
                    userId,
                    "USER",
                    user.getFullName(),
                    tenantId,
                    "TENANT",
                    tenantId,
                    "FAILURE",
                    ipAddress,
                    "{\"reason\":\"El usuario no pertenece a este Tenant\"}"
                );
                return new TenantAccessDeniedException("No posee una membresía activa en la organización seleccionada.");
            });

        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        extractRolesAndPermissionsFromMembership(membership, roles, permissions);

        // Generar nuevo JWT Access Token con el nuevo tenantId
        String accessToken = jwtProvider.generateAccessToken(
            user.getId(), user.getEmail(), user.getFullName(), tenantId, roles, permissions
        );

        List<MembershipJpaEntity> activeMemberships = membershipRepository.findByUserIdAndStatus(user.getId(), "ACTIVE");
        List<TenantSummaryResponse> availableTenants = buildAvailableTenants(activeMemberships);

        auditService.logSecurityEvent(
            "TENANT_SWITCHED",
            userId,
            "USER",
            user.getFullName(),
            tenantId,
            "TENANT",
            tenantId,
            "SUCCESS",
            ipAddress,
            "{\"targetTenant\":\"" + tenantId + "\"}"
        );

        return new AuthResponse(
            accessToken,
            null, // No es necesario cambiar el refresh token
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            tenantId,
            roles,
            permissions,
            availableTenants
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshTokenJpaEntity tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BadCredentialsException("Refresh Token inválido"));

        if (!tokenEntity.isValid()) {
            throw new BadCredentialsException("Refresh Token ha expirado o ya fue utilizado");
        }

        // Refresh Token Rotation: marcar actual como usado
        tokenEntity.setUsed(true);
        refreshTokenRepository.save(tokenEntity);

        UserJpaEntity user = tokenEntity.getUser();

        List<MembershipJpaEntity> activeMemberships = membershipRepository.findByUserIdAndStatus(user.getId(), "ACTIVE");
        List<TenantSummaryResponse> availableTenants = buildAvailableTenants(activeMemberships);

        String defaultTenantId = activeMemberships.size() == 1 ? activeMemberships.get(0).getTenantId() : null;
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        if (defaultTenantId != null) {
            extractRolesAndPermissions(user.getId(), defaultTenantId, roles, permissions);
        }

        String newAccessToken = jwtProvider.generateAccessToken(
            user.getId(), user.getEmail(), user.getFullName(), defaultTenantId, roles, permissions
        );

        String newRefreshTokenValue = generateAndSaveRefreshToken(user, ipAddress);

        return new AuthResponse(
            newAccessToken,
            newRefreshTokenValue,
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            defaultTenantId,
            roles,
            permissions,
            availableTenants
        );
    }

    @Transactional
    public void logout(String userId) {
        if (userId != null) {
            refreshTokenRepository.deleteByUserId(userId);
        }
    }

    // ===================================================
    // Métodos auxiliares de extracción de Roles y Permisos
    // ===================================================

    private void extractRolesAndPermissions(String userId, String tenantId, List<String> roles, List<String> permissions) {
        Optional<MembershipJpaEntity> mOpt = membershipRepository.findActiveMembershipWithRolesAndPermissions(userId, tenantId);
        if (mOpt.isPresent()) {
            extractRolesAndPermissionsFromMembership(mOpt.get(), roles, permissions);
        }
    }

    private void extractRolesAndPermissionsFromMembership(MembershipJpaEntity membership, List<String> roles, List<String> permissions) {
        if (membership.getRoles() != null) {
            for (RoleJpaEntity role : membership.getRoles()) {
                roles.add(role.getName());
                if (role.getPermissions() != null) {
                    for (RolePermissionJpaEntity perm : role.getPermissions()) {
                        String permStr = perm.toPermissionString();
                        if (!permissions.contains(permStr)) {
                            permissions.add(permStr);
                        }
                    }
                }
            }
        }
    }

    private List<TenantSummaryResponse> buildAvailableTenants(List<MembershipJpaEntity> memberships) {
        List<TenantSummaryResponse> list = new ArrayList<>();
        for (MembershipJpaEntity m : memberships) {
            Optional<TenantJpaEntity> tOpt = tenantRepository.findById(m.getTenantId());
            if (tOpt.isPresent()) {
                TenantJpaEntity t = tOpt.get();
                list.add(new TenantSummaryResponse(
                    t.getId(),
                    t.getLegalName(),
                    t.getCommercialName(),
                    m.getStatus(),
                    t.getServicePlan()
                ));
            }
        }
        return list;
    }

    private String generateAndSaveRefreshToken(UserJpaEntity user, String ipAddress) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        RefreshTokenJpaEntity tokenEntity = new RefreshTokenJpaEntity();
        tokenEntity.setId(UUID.randomUUID().toString());
        tokenEntity.setUser(user);
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 días de vigencia
        tokenEntity.setUsed(false);
        tokenEntity.setCreatedFromIp(ipAddress);

        refreshTokenRepository.save(tokenEntity);
        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculando hash SHA-256", e);
        }
    }
}

package com.certidigital.platform.iam.application.service;

import com.certidigital.platform.iam.application.dto.TenantSummaryResponse;
import com.certidigital.platform.iam.application.dto.UserProfileResponse;
import com.certidigital.platform.iam.infrastructure.persistence.*;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaEntity;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserApplicationService {

    private final UserJpaRepository userRepository;
    private final MembershipJpaRepository membershipRepository;
    private final TenantJpaRepository tenantRepository;

    public UserApplicationService(
        UserJpaRepository userRepository,
        MembershipJpaRepository membershipRepository,
        TenantJpaRepository tenantRepository
    ) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String userId) {
        UserJpaEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        String activeTenantId = TenantContextHolder.getTenantId();
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        if (activeTenantId != null && !activeTenantId.isBlank()) {
            Optional<MembershipJpaEntity> mOpt = membershipRepository.findActiveMembershipWithRolesAndPermissions(userId, activeTenantId);
            if (mOpt.isPresent()) {
                MembershipJpaEntity m = mOpt.get();
                if (m.getRoles() != null) {
                    for (RoleJpaEntity role : m.getRoles()) {
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
        }

        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getStatus(),
            user.getEmailVerified(),
            user.getLastLoginAt(),
            activeTenantId,
            roles,
            permissions
        );
    }

    @Transactional(readOnly = true)
    public List<TenantSummaryResponse> getUserTenants(String userId) {
        List<MembershipJpaEntity> memberships = membershipRepository.findByUserIdAndStatus(userId, "ACTIVE");
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
}

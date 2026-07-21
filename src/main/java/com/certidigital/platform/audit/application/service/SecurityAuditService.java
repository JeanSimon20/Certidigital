package com.certidigital.platform.audit.application.service;

import com.certidigital.platform.audit.infrastructure.persistence.AuditEntryJpaEntity;
import com.certidigital.platform.audit.infrastructure.persistence.AuditEntryJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SecurityAuditService {

    private final AuditEntryJpaRepository auditRepository;

    public SecurityAuditService(AuditEntryJpaRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void logSecurityEvent(
        String actionCode,
        String actorId,
        String actorType,
        String actorName,
        String tenantId,
        String resourceType,
        String resourceId,
        String result,
        String ipAddress,
        String payload
    ) {
        AuditEntryJpaEntity entry = new AuditEntryJpaEntity();
        entry.setId(UUID.randomUUID().toString());
        entry.setActionCode(actionCode);
        entry.setActorId(actorId);
        entry.setActorType(actorType != null ? actorType : "USER");
        entry.setActorName(actorName);
        entry.setTenantId(tenantId);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setResult(result != null ? result : "SUCCESS");
        entry.setIpAddress(ipAddress);
        entry.setPayload(payload);
        entry.setOccurredAt(LocalDateTime.now());

        auditRepository.save(entry);
    }
}

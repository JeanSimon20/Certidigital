package com.certidigital.platform.audit.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryJpaRepository extends JpaRepository<AuditEntryJpaEntity, String> {
}

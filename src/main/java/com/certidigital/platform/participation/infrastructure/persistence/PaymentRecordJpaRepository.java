package com.certidigital.platform.participation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRecordJpaRepository extends JpaRepository<PaymentRecordJpaEntity, String> {

    List<PaymentRecordJpaEntity> findAllByEnrollmentId(String enrollmentId);

    List<PaymentRecordJpaEntity> findAllByTenantId(String tenantId);
}

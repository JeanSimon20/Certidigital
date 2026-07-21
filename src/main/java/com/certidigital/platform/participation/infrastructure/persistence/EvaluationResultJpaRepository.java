package com.certidigital.platform.participation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationResultJpaRepository extends JpaRepository<EvaluationResultJpaEntity, String> {

    List<EvaluationResultJpaEntity> findAllByEnrollmentId(String enrollmentId);

    List<EvaluationResultJpaEntity> findAllByTenantId(String tenantId);
}

package com.certidigital.platform.policy.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EligibilityEvaluationJpaRepository extends JpaRepository<EligibilityEvaluationJpaEntity, String> {

    List<EligibilityEvaluationJpaEntity> findAllByTenantIdAndEnrollmentId(String tenantId, String enrollmentId);

    List<EligibilityEvaluationJpaEntity> findAllByTenantId(String tenantId);
}

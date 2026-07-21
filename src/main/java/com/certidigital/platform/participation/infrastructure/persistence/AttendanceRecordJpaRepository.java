package com.certidigital.platform.participation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecordJpaEntity, String> {

    boolean existsByEnrollmentIdAndSessionId(String enrollmentId, String sessionId);

    Optional<AttendanceRecordJpaEntity> findByEnrollmentIdAndSessionId(String enrollmentId, String sessionId);

    List<AttendanceRecordJpaEntity> findAllByEnrollmentId(String enrollmentId);

    List<AttendanceRecordJpaEntity> findAllByTenantIdAndSessionId(String tenantId, String sessionId);
}

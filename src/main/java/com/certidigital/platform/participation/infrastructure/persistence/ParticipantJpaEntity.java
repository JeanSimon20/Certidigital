package com.certidigital.platform.participation.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "participants",
    indexes = {
        @Index(name = "idx_participants_email",   columnList = "email"),
        @Index(name = "idx_participants_doc",     columnList = "doc_type, doc_number, doc_country"),
        @Index(name = "idx_participants_user_id", columnList = "identity_user_id")
    }
)
public class ParticipantJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", length = 500, nullable = false)
    private String fullName;

    @Column(name = "doc_type", length = 50)
    private String docType;

    @Column(name = "doc_number", length = 100)
    private String docNumber;

    @Column(name = "doc_country", length = 3)
    private String docCountry;

    @Column(name = "identity_status", length = 30, nullable = false)
    private String identityStatus = "UNVERIFIED";

    @Column(name = "identity_user_id", length = 36)
    private String identityUserId;

    @Column(name = "phone", length = 50)
    private String phone;

    @OneToMany(
        mappedBy = "participant",
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<EnrollmentJpaEntity> enrollments = new ArrayList<>();

    public ParticipantJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }

    public String getDocCountry() { return docCountry; }
    public void setDocCountry(String docCountry) { this.docCountry = docCountry; }

    public String getIdentityStatus() { return identityStatus; }
    public void setIdentityStatus(String identityStatus) { this.identityStatus = identityStatus; }

    public String getIdentityUserId() { return identityUserId; }
    public void setIdentityUserId(String identityUserId) { this.identityUserId = identityUserId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<EnrollmentJpaEntity> getEnrollments() { return enrollments; }
    public void setEnrollments(List<EnrollmentJpaEntity> enrollments) { this.enrollments = enrollments; }
}

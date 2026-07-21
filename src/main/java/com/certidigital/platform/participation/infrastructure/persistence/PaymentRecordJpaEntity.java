package com.certidigital.platform.participation.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payment_records",
    indexes = {
        @Index(name = "idx_payment_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_payment_tenant",     columnList = "tenant_id"),
        @Index(name = "idx_payment_status",     columnList = "payment_status")
    }
)
public class PaymentRecordJpaEntity extends AuditableEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_payment_enrollment"))
    private EnrollmentJpaEntity enrollment;

    @Column(name = "payment_status", length = 30, nullable = false)
    private String paymentStatus = "PENDING";

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "external_reference", length = 500)
    private String externalReference;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "operation_number", length = 100)
    private String operationNumber;

    @Column(name = "receipt_url", columnDefinition = "TEXT")
    private String receiptUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmed_by", length = 36)
    private String confirmedBy;

    public PaymentRecordJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public EnrollmentJpaEntity getEnrollment() { return enrollment; }
    public void setEnrollment(EnrollmentJpaEntity enrollment) { this.enrollment = enrollment; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOperationNumber() { return operationNumber; }
    public void setOperationNumber(String operationNumber) { this.operationNumber = operationNumber; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(String confirmedBy) { this.confirmedBy = confirmedBy; }
}

package com.certidigital.platform.payment.presentation;

import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.service.PaymentApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.certidigital.platform.payment.application.dto.SubmitVoucherRequest;
import com.certidigital.platform.payment.application.dto.VerifyPaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentVerificationResponse;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentApplicationService paymentService;

    public PaymentController(PaymentApplicationService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResult>> processPayment(
        @RequestBody PaymentRequest request,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        PaymentResult result = paymentService.processEnrollmentPayment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "Procesamiento de pago completado"));
    }

    @PostMapping("/submit-voucher")
    public ResponseEntity<ApiResponse<PaymentResult>> submitVoucher(
        @RequestBody SubmitVoucherRequest request,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        PaymentResult result = paymentService.submitVoucher(request, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "Comprobante de pago enviado a verificación"));
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<ApiResponse<List<PaymentVerificationResponse>>> getPendingVerifications() {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "tenant-001-aaaa-bbbb-cccc-dddddddd";
        }
        List<PaymentVerificationResponse> list = paymentService.getPendingVerificationsForTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(list, "Solicitudes de comprobantes pendientes obtenidas"));
    }

    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<ApiResponse<PaymentVerificationResponse>> verifyPayment(
        @PathVariable String paymentId,
        @RequestBody VerifyPaymentRequest request,
        Authentication authentication
    ) {
        String adminUserId = authentication.getName();
        PaymentVerificationResponse result = paymentService.verifyPaymentVoucher(paymentId, request, adminUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Verificación de comprobante registrada exitosamente"));
    }
}

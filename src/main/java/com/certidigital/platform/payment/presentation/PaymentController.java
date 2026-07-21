package com.certidigital.platform.payment.presentation;

import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.service.PaymentApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}

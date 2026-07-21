package com.certidigital.platform.payment.infrastructure.adapter;

import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.port.PaymentGatewayPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentSimulatorAdapter implements PaymentGatewayPort {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        if (request.isSimulateFailure()) {
            String reason = request.getFailureReason() != null ? request.getFailureReason() : "Fondos insuficientes o tarjeta rechazada";
            return PaymentResult.failure(reason);
        }

        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return PaymentResult.failure("El monto del pago no puede ser negativo");
        }

        String externalRef = "SIM-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String receiptUrl = "https://certidigital.platform/receipts/" + externalRef;

        return PaymentResult.success(externalRef, receiptUrl);
    }

    @Override
    public PaymentResult refundPayment(String externalReference, BigDecimal amount) {
        String refundRef = "SIM-REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, "REFUNDED", refundRef, null, null);
    }
}

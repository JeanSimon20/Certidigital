package com.certidigital.platform.payment.application.port;

import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;

/**
 * PaymentGatewayPort — Puerto de abstracción para pasarelas de pago.
 *
 * Permite desacoplar el dominio de proveedores específicos (Stripe, Culqi, Niubiz, Simulador).
 */
public interface PaymentGatewayPort {

    PaymentResult processPayment(PaymentRequest request);

    PaymentResult refundPayment(String externalReference, java.math.BigDecimal amount);
}

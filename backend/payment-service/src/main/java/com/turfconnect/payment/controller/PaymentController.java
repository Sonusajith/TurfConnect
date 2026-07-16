package com.turfconnect.payment.controller;

import com.turfconnect.payment.service.PaymentService;
import com.turfconnect.payment.service.RefundService;
import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.RefundRequest;
import com.turfconnect.shared.dto.payment.RefundResponse;
import com.turfconnect.shared.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @Value("${spring.security.internal-token:internal-secret-token}")
    private String internalToken;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PaymentInitiateRequest request) {

        PaymentResponse response = paymentService.initiatePayment(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @RequestParam("transactionId") String transactionId) {

        PaymentResponse response = paymentService.verifyPayment(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers) {

        String signatureHeader = null;
        String upperProvider = provider.toUpperCase();

        if ("STRIPE".equals(upperProvider)) {
            signatureHeader = headers.getOrDefault("stripe-signature", headers.get("Stripe-Signature"));
        } else if ("RAZORPAY".equals(upperProvider)) {
            signatureHeader = headers.getOrDefault("x-razorpay-signature", headers.get("X-Razorpay-Signature"));
        } else if ("MOCK".equals(upperProvider)) {
            signatureHeader = headers.getOrDefault("x-mock-signature", headers.get("X-Mock-Signature"));
        }

        log.info("Received webhook callback from provider: {}, signature present: {}", provider, signatureHeader != null);
        paymentService.processWebhook(provider, payload, signatureHeader);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/v1/payments/refund
     *
     * Internal-only endpoint. Only booking-service may call this using X-Internal-Token.
     * Initiates a refund for a cancelled booking. Idempotent: duplicate calls for the
     * same bookingId return the existing refund state without re-triggering the gateway.
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @RequestHeader("X-Internal-Token") String token,
            @Valid @RequestBody RefundRequest request) {

        if (!internalToken.equals(token)) {
            throw new BadRequestException("Unauthorized: invalid internal service token");
        }

        RefundResponse response = refundService.initiateRefund(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/payments/booking/{bookingId}
     *
     * Internal-only. Returns the current payment record for a booking.
     * Used by booking-service to check if payment is SUCCESS before triggering refund.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingId(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable String bookingId) {

        if (!internalToken.equals(token)) {
            throw new BadRequestException("Unauthorized: invalid internal service token");
        }

        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.turfconnect.payment.controller;

import com.turfconnect.payment.service.PaymentService;
import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // Retrieve provider-specific signature header dynamically
        if ("STRIPE".equals(upperProvider)) {
            signatureHeader = headers.get("stripe-signature");
            if (signatureHeader == null) {
                signatureHeader = headers.get("Stripe-Signature");
            }
        } else if ("RAZORPAY".equals(upperProvider)) {
            signatureHeader = headers.get("x-razorpay-signature");
            if (signatureHeader == null) {
                signatureHeader = headers.get("X-Razorpay-Signature");
            }
        } else if ("MOCK".equals(upperProvider)) {
            signatureHeader = headers.get("x-mock-signature");
            if (signatureHeader == null) {
                signatureHeader = headers.get("X-Mock-Signature");
            }
        }

        log.info("Received webhook callback from provider: {}, signature present: {}", provider, signatureHeader != null);

        paymentService.processWebhook(provider, payload, signatureHeader);
        return ResponseEntity.ok().build();
    }
}

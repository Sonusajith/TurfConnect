package com.turfconnect.payment.strategy;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component("STRIPE")
@Slf4j
public class StripePaymentStrategy implements PaymentGatewayStrategy {

    @Value("${stripe.api-key:}")
    private String apiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentResponse initiate(PaymentInitiateRequest request) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("your_stripe")) {
            throw new BadRequestException("Stripe gateway is not configured. Please supply a valid API key.");
        }

        Stripe.apiKey = apiKey;

        // Stripe expects amount in cents (e.g. $10.00 is 1000 cents)
        long amountCents = request.getAmount().multiply(new BigDecimal("100")).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(request.getCurrency().toLowerCase())
                .putMetadata("bookingId", request.getBookingId())
                .putMetadata("provider", "STRIPE")
                .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return PaymentResponse.builder()
                    .bookingId(request.getBookingId())
                    .transactionId(paymentIntent.getId())
                    .paymentReference(paymentIntent.getId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .provider("STRIPE")
                    .status(PaymentStatus.PENDING)
                    .clientSecret(paymentIntent.getClientSecret())
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Stripe payment initiation failed", e);
            throw new BadRequestException("Failed to initiate Stripe payment: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            log.warn("Stripe webhook secret is empty. Signature verification skipped (unsafe).");
            return true;
        }
        try {
            // Default tolerance is 300 seconds (5 minutes)
            return Webhook.Signature.verifyHeader(payload, signatureHeader, webhookSecret, 300);
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed", e);
            return false;
        }
    }

    @Override
    public PaymentStatus processWebhookEvent(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);
            String type = root.path("type").asText();

            if ("payment_intent.succeeded".equals(type)) {
                return PaymentStatus.SUCCESS;
            } else if ("payment_intent.payment_failed".equals(type)) {
                return PaymentStatus.FAILED;
            } else if ("payment_intent.processing".equals(type)) {
                return PaymentStatus.PROCESSING;
            }
        } catch (Exception e) {
            log.error("Error parsing Stripe webhook payload using Jackson", e);
        }
        return PaymentStatus.PENDING;
    }
}

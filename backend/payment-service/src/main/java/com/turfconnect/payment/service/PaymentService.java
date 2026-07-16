package com.turfconnect.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turfconnect.payment.model.Payment;
import com.turfconnect.payment.repository.PaymentRepository;
import com.turfconnect.payment.strategy.PaymentGatewayStrategy;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentGatewayStrategy> gatewayStrategies;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${services.booking-service.url:http://localhost:8083}")
    private String bookingServiceUrl;

    @Value("${spring.security.internal-token:internal-secret-token}")
    private String internalTokenSecret;

    public PaymentResponse initiatePayment(PaymentInitiateRequest request, String idempotencyKey) {
        // 1. Idempotency check: lookup key if present
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            Payment existing = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (existing != null) {
                log.info("Duplicate request detected. Returning existing payment record for key: {}", idempotencyKey);
                return toPaymentResponse(existing);
            }
        }

        // 2. Resolve Strategy
        String provider = request.getProvider().toUpperCase();
        PaymentGatewayStrategy strategy = gatewayStrategies.get(provider);
        if (strategy == null) {
            throw new BadRequestException("Unsupported payment provider: " + provider);
        }

        // 3. Initiate payment via strategy
        PaymentResponse gatewayResponse = strategy.initiate(request);

        // 4. Create and save entity
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .transactionId(gatewayResponse.getTransactionId())
                .paymentReference(gatewayResponse.getPaymentReference())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        try {
            Payment saved = paymentRepository.save(payment);
            return toPaymentResponse(saved);
        } catch (DuplicateKeyException e) {
            // Concurrent double click recovery fallback
            Payment existing = paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new BadRequestException("Duplicate request encountered."));
            return toPaymentResponse(existing);
        }
    }

    public PaymentResponse verifyPayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for transaction ID: " + transactionId));

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return toPaymentResponse(payment);
        }

        // Simulating manual verification success
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        // Notify Booking Service via secure REST call
        updateBookingStatus(payment.getBookingId(), true);

        // Publish payment success event to RabbitMQ
        publishPaymentEvent(saved);

        return toPaymentResponse(saved);
    }

    public void processWebhook(String provider, String payload, String signatureHeader) {
        String upperProvider = provider.toUpperCase();
        PaymentGatewayStrategy strategy = gatewayStrategies.get(upperProvider);
        if (strategy == null) {
            throw new BadRequestException("Unsupported webhook provider: " + provider);
        }

        // 1. Verify Signature
        boolean isValid = strategy.verifyWebhookSignature(payload, signatureHeader);
        if (!isValid) {
            throw new BadRequestException("Webhook signature verification failed.");
        }

        // 2. Parse status outcome
        PaymentStatus webhookStatus = strategy.processWebhookEvent(payload);

        // 3. Extract transaction identifier
        String transactionId = extractTransactionId(upperProvider, payload);
        if (transactionId == null) {
            log.error("Could not extract transaction ID from webhook payload.");
            return;
        }

        Payment payment = paymentRepository.findByTransactionId(transactionId).orElse(null);
        if (payment == null) {
            log.warn("Payment record not found for webhook transaction ID: {}", transactionId);
            return;
        }

        // 4. Avoid processing duplicate hook deliveries
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Payment {} is already in final state: {}", transactionId, payment.getStatus());
            return;
        }

        // 5. Update and transition status
        payment.setStatus(webhookStatus);
        if (webhookStatus == PaymentStatus.SUCCESS || webhookStatus == PaymentStatus.FAILED) {
            payment.setCompletedAt(LocalDateTime.now());
        }
        Payment saved = paymentRepository.save(payment);

        // Publish payment status change event to RabbitMQ
        publishPaymentEvent(saved);

        // 6. Notify Booking Service
        if (webhookStatus == PaymentStatus.SUCCESS) {
            updateBookingStatus(payment.getBookingId(), true);
        } else if (webhookStatus == PaymentStatus.FAILED) {
            updateBookingStatus(payment.getBookingId(), false);
        }
    }

    private String extractTransactionId(String provider, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if ("STRIPE".equals(provider)) {
                return root.path("data").path("object").path("id").asText();
            } else if ("RAZORPAY".equals(provider)) {
                // Razorpay standard structure for payment.captured / payment.failed webhook events
                return root.path("payload").path("payment").path("entity").path("order_id").asText();
            } else if ("MOCK".equals(provider)) {
                return root.path("transactionId").asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse transaction ID from payload", e);
        }
        return null;
    }

    private void updateBookingStatus(String bookingId, boolean success) {
        String action = success ? "confirm" : "cancel";
        String url = bookingServiceUrl + "/api/v1/bookings/" + bookingId + "/" + action;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", internalTokenSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (Exception e) {
            log.error("Failed to propagate booking state confirmation to booking-service", e);
            throw new BadRequestException("Booking service is currently unavailable. Rollback or queue retry required.");
        }
    }

    private void publishPaymentEvent(Payment payment) {
        try {
            String eventType = payment.getStatus() == PaymentStatus.SUCCESS ? "SUCCESS" : "FAILED";
            com.turfconnect.shared.dto.event.PaymentEvent event = com.turfconnect.shared.dto.event.PaymentEvent.builder()
                    .transactionId(payment.getTransactionId())
                    .bookingId(payment.getBookingId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(com.turfconnect.payment.config.RabbitMQConfig.PAYMENT_EXCHANGE, "payment." + eventType.toLowerCase(), event);
            log.info("Successfully published payment event {} for transaction {}", eventType, payment.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish payment event", e);
        }
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .transactionId(payment.getTransactionId())
                .paymentReference(payment.getPaymentReference())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .provider(payment.getProvider())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}

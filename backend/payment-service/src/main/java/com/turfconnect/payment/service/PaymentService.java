package com.turfconnect.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turfconnect.payment.model.Payment;
import com.turfconnect.payment.repository.PaymentRepository;
import com.turfconnect.payment.strategy.PaymentGatewayStrategy;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.dto.payment.PaymentVerifyRequest;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    @Value("${razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:}")
    private String razorpayKeySecret;

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
        return verifyPayment(PaymentVerifyRequest.builder().transactionId(transactionId).build());
    }

    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {
        if (request == null || request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()) {
            throw new BadRequestException("transactionId is required for payment verification.");
        }

        String transactionId = request.getTransactionId().trim();
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for transaction ID: " + transactionId));

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return toPaymentResponse(payment);
        }

        if ("RAZORPAY".equalsIgnoreCase(payment.getProvider())) {
            verifyRazorpayCheckoutPayment(payment, request);
            payment.setProviderPaymentId(request.getRazorpayPaymentId());
            payment.setGatewaySignature(request.getRazorpaySignature());
            payment.setPaymentReference(request.getRazorpayPaymentId());
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        // Notify Booking Service via secure REST call
        updateBookingStatus(payment.getBookingId(), true);

        // Publish payment success event to RabbitMQ
        publishPaymentEvent(saved);

        return toPaymentResponse(saved);
    }

    private void verifyRazorpayCheckoutPayment(Payment payment, PaymentVerifyRequest request) {
        String orderId = trimToNull(request.getRazorpayOrderId());
        String paymentId = trimToNull(request.getRazorpayPaymentId());
        String signature = trimToNull(request.getRazorpaySignature());

        if (orderId == null || paymentId == null || signature == null) {
            throw new BadRequestException("Razorpay payment id, order id, and signature are required.");
        }
        if (!orderId.equals(payment.getTransactionId())) {
            throw new BadRequestException("Razorpay order does not match the saved payment order.");
        }
        if (razorpayKeySecret == null || razorpayKeySecret.trim().isEmpty() || razorpayKeySecret.contains("your_razorpay")) {
            throw new BadRequestException("Razorpay gateway is not configured. Please supply a valid Key Secret.");
        }

        String payload = orderId + "|" + paymentId;
        String expectedSignature = hmacSha256Hex(payload, razorpayKeySecret);
        if (!constantTimeEquals(expectedSignature, signature)) {
            throw new BadRequestException("Razorpay payment signature verification failed.");
        }
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Razorpay checkout signature calculation failed", e);
            throw new BadRequestException("Payment signature verification failed.");
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
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
                .orderId("RAZORPAY".equalsIgnoreCase(payment.getProvider()) ? payment.getTransactionId() : null)
                .keyId("RAZORPAY".equalsIgnoreCase(payment.getProvider()) ? razorpayKeyId : null)
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

    /**
     * Returns the current payment record for a booking.
     * Used internally by booking-service before triggering a refund.
     */
    public PaymentResponse getPaymentByBookingId(String bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for bookingId: " + bookingId));
        return toPaymentResponse(payment);
    }
}

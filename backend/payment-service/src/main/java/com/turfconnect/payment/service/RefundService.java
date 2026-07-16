package com.turfconnect.payment.service;

import com.turfconnect.payment.config.RabbitMQConfig;
import com.turfconnect.payment.model.Payment;
import com.turfconnect.payment.model.Refund;
import com.turfconnect.payment.repository.PaymentRepository;
import com.turfconnect.shared.dto.event.PaymentEvent;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.dto.payment.RefundRequest;
import com.turfconnect.shared.dto.payment.RefundResponse;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefundService orchestrates the full refund lifecycle:
 *
 *  Payment(SUCCESS)
 *     └─► REFUND_INITIATED   (validated, refund record created)
 *         └─► REFUND_PROCESSING (gateway call in progress — simulated synchronously here)
 *             ├─► REFUNDED       (gateway confirmed settlement)
 *             └─► REFUND_FAILED  (gateway rejected or timeout)
 *
 * Idempotency: keyed on bookingId — a duplicate call for the same bookingId
 * returns the existing refund state without re-triggering the gateway.
 *
 * In Module 11 only FULL refunds are implemented. The Refund model is
 * designed to support PARTIAL refunds in a future module without schema changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Initiate a refund for a booking.
     *
     * Validation sequence (fail-fast):
     *  1. Payment record exists for this bookingId.
     *  2. Booking status is CANCELLED (checked by caller — booking-service passes this implicitly).
     *  3. Payment status is SUCCESS (only successfully charged payments can be refunded).
     *  4. Payment has not already entered the refund lifecycle (idempotency).
     *  5. Refund amount does not exceed original payment amount.
     */
    public RefundResponse initiateRefund(RefundRequest request) {
        String bookingId = request.getBookingId();
        log.info("Refund requested for bookingId={}, initiatedBy={}", bookingId, request.getInitiatedBy());

        // --- 1. Lookup payment ---
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment record found for bookingId: " + bookingId));

        // --- 2. Idempotency check ---
        // If a refund is already in progress or completed, return the current state.
        if (isRefundLifecycleActive(payment.getStatus())) {
            log.info("Refund already active for bookingId={}. Status={}. Returning existing record.",
                    bookingId, payment.getStatus());
            return toRefundResponse(payment);
        }

        // --- 3. Validate payment is in SUCCESS state ---
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException(
                    "Refund can only be initiated for payments in SUCCESS state. " +
                    "Current status: " + payment.getStatus());
        }

        // --- 4. Validate refund amount does not exceed original (full-refund guard) ---
        BigDecimal refundAmount = payment.getAmount(); // Always full refund in Module 11
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException(
                    "Refund amount (" + refundAmount + ") exceeds original payment amount (" + payment.getAmount() + ")");
        }

        // --- 5. Build refund sub-document and transition to REFUND_INITIATED ---
        String refundId = UUID.randomUUID().toString();
        String refundReference = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Refund refund = Refund.builder()
                .refundId(refundId)
                .refundReference(refundReference)
                .refundType("FULL")
                .refundAmount(refundAmount)
                .remainingAmount(BigDecimal.ZERO)
                .reason(request.getReason())
                .initiatedBy(request.getInitiatedBy() != null ? request.getInitiatedBy() : "SYSTEM")
                .initiatedAt(LocalDateTime.now())
                .build();

        payment.setRefund(refund);
        payment.setStatus(PaymentStatus.REFUND_INITIATED);
        payment.setRefundInitiatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        log.info("Refund INITIATED for bookingId={}, refundId={}, refundReference={}",
                bookingId, refundId, refundReference);

        // --- 6. Simulate gateway refund call (REFUND_PROCESSING → REFUNDED) ---
        // In production this would be async via webhook; here we simulate synchronous settlement.
        payment = processGatewayRefund(payment, refundAmount);

        // --- 7. Publish RabbitMQ event ---
        publishRefundEvent(payment);

        return toRefundResponse(payment);
    }

    /**
     * Simulates calling the payment gateway to process the refund.
     * Transitions: REFUND_INITIATED → REFUND_PROCESSING → REFUNDED (or REFUND_FAILED on error).
     *
     * In a production async gateway (e.g., Stripe), REFUND_PROCESSING would be saved
     * and REFUNDED would be set upon receiving a webhook callback.
     */
    private Payment processGatewayRefund(Payment payment, BigDecimal refundAmount) {
        try {
            // Mark as processing (gateway call in progress)
            payment.setStatus(PaymentStatus.REFUND_PROCESSING);
            paymentRepository.save(payment);
            log.info("Refund PROCESSING for bookingId={}", payment.getBookingId());

            // === Mock gateway refund call ===
            // Real implementation would call strategy.refund(refundReference, refundAmount)
            // and handle the gateway's response object.
            String gatewayReference = "GW-REF-" + System.currentTimeMillis();

            // Settlement confirmed — mark REFUNDED
            payment.getRefund().setCompletedAt(LocalDateTime.now());
            payment.getRefund().setGatewayReference(gatewayReference);
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("Refund REFUNDED for bookingId={}, gatewayReference={}", payment.getBookingId(), gatewayReference);

        } catch (Exception e) {
            // Gateway failure — mark REFUND_FAILED and persist failure reason for audit
            log.error("Refund gateway call FAILED for bookingId={}: {}", payment.getBookingId(), e.getMessage());
            payment.getRefund().setFailureReason(e.getMessage());
            payment.setStatus(PaymentStatus.REFUND_FAILED);
            paymentRepository.save(payment);
        }
        return payment;
    }

    /**
     * Publishes a PaymentEvent to RabbitMQ when a refund is completed or fails.
     * Uses the existing payment.exchange with routing key payment.refunded or payment.refund_failed.
     * Notification-service listens to payment.notification.queue which is bound to payment.*.
     */
    private void publishRefundEvent(Payment payment) {
        try {
            String eventType = payment.getStatus() == PaymentStatus.REFUNDED ? "REFUNDED" : "REFUND_FAILED";
            PaymentEvent event = PaymentEvent.builder()
                    .transactionId(payment.getTransactionId())
                    .bookingId(payment.getBookingId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .refundAmount(payment.getRefund() != null ? payment.getRefund().getRefundAmount() : null)
                    .refundReference(payment.getRefund() != null ? payment.getRefund().getRefundReference() : null)
                    .build();

            // Routing key: payment.refunded or payment.refund_failed
            String routingKey = "payment." + eventType.toLowerCase();
            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, routingKey, event);
            log.info("Published refund event [{}] for bookingId={}", eventType, payment.getBookingId());
        } catch (Exception e) {
            // Non-fatal: log the failure but do not roll back the refund state
            log.error("Failed to publish refund event for bookingId={}: {}", payment.getBookingId(), e.getMessage());
        }
    }

    /**
     * Returns true if the payment is already in any refund lifecycle state,
     * meaning we should not re-initiate the refund (idempotency guard).
     */
    private boolean isRefundLifecycleActive(PaymentStatus status) {
        return status == PaymentStatus.REFUND_INITIATED
                || status == PaymentStatus.REFUND_PROCESSING
                || status == PaymentStatus.REFUNDED
                || status == PaymentStatus.REFUND_FAILED;
    }

    /**
     * Maps a Payment + embedded Refund into a RefundResponse DTO.
     */
    private RefundResponse toRefundResponse(Payment payment) {
        Refund refund = payment.getRefund();
        return RefundResponse.builder()
                .refundId(refund != null ? refund.getRefundId() : null)
                .refundReference(refund != null ? refund.getRefundReference() : null)
                .bookingId(payment.getBookingId())
                .paymentId(payment.getId())
                .refundAmount(refund != null ? refund.getRefundAmount() : null)
                .originalAmount(payment.getAmount())
                .currency(payment.getCurrency())
                .refundType(refund != null ? refund.getRefundType() : null)
                .remainingAmount(refund != null ? refund.getRemainingAmount() : null)
                .status(payment.getStatus())
                .reason(refund != null ? refund.getReason() : null)
                .initiatedBy(refund != null ? refund.getInitiatedBy() : null)
                .failureReason(refund != null ? refund.getFailureReason() : null)
                .initiatedAt(refund != null ? refund.getInitiatedAt() : null)
                .completedAt(refund != null ? refund.getCompletedAt() : null)
                .build();
    }
}

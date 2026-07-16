package com.turfconnect.shared.dto.event;

import com.turfconnect.shared.dto.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a payment is processed (SUCCESS, FAILED)
 * or a refund is processed (REFUNDED, REFUND_FAILED).
 * Delivered via RabbitMQ to notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String transactionId;
    private String bookingId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String eventType;         // "SUCCESS", "FAILED", "REFUNDED", "REFUND_FAILED"
    private LocalDateTime timestamp;

    // Refund-specific fields (null for non-refund events)
    private BigDecimal refundAmount;   // Amount refunded to the user
    private String refundReference;    // Gateway-level refund reference for reconciliation
}

package com.turfconnect.shared.dto.event;

import com.turfconnect.shared.dto.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a payment is processed (SUCCESS, FAILED).
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
    private String eventType; // "SUCCESS", "FAILED"
    private LocalDateTime timestamp;
}

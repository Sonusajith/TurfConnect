package com.turfconnect.shared.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String bookingId;
    private String transactionId;
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private String provider;
    private PaymentStatus status;
    private String failureReason;
    private String clientSecret;
    private String orderId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

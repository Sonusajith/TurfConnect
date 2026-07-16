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
public class RefundResponse {

    private String refundId;          // Unique UUID for this refund record
    private String refundReference;   // Gateway-level reference for reconciliation
    private String bookingId;
    private String paymentId;

    private BigDecimal refundAmount;
    private BigDecimal originalAmount;
    private String currency;

    // FULL is the only implemented type in Module 11;
    // PARTIAL is modelled here for future readiness
    private String refundType;        // "FULL" | "PARTIAL"
    private BigDecimal remainingAmount;

    private PaymentStatus status;     // REFUND_INITIATED | REFUND_PROCESSING | REFUNDED | REFUND_FAILED

    private String reason;
    private String initiatedBy;
    private String failureReason;

    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
}

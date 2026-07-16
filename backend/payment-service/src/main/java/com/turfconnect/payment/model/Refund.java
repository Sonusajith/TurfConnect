package com.turfconnect.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Embedded sub-document representing a refund record inside a Payment document.
 * Not stored as a separate collection — embedded to keep refund lifecycle
 * atomically tied to its parent payment.
 *
 * Partial-refund-ready: refundAmount and remainingAmount are modelled now,
 * but only full refunds are implemented in Module 11.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    // Unique identifier for this refund record (UUID)
    private String refundId;

    // Unique reference sent to / received from the payment gateway for this refund.
    // Used for idempotency on gateway calls and future reconciliation.
    private String refundReference;

    // "FULL" or "PARTIAL" — only FULL is implemented in Module 11
    private String refundType;

    // The actual amount being refunded (equals original amount for FULL refunds)
    private BigDecimal refundAmount;

    // Amount still owed to the customer after this refund (0 for FULL refunds)
    private BigDecimal remainingAmount;

    // Optional human-readable reason (e.g., "Venue unavailable", "User cancelled")
    private String reason;

    // === Audit Trail (required for customer support and viva explanation) ===

    // Who triggered the refund: "SYSTEM" (auto on cancel) or a userId for manual cases
    private String initiatedBy;

    // When the refund was first requested
    private LocalDateTime initiatedAt;

    // When the gateway confirmed the refund was settled (null until REFUNDED state)
    private LocalDateTime completedAt;

    // If the gateway returned a reference ID, store it here for reconciliation
    private String gatewayReference;

    // If refund failed, the reason from the gateway or internal error message
    private String failureReason;
}

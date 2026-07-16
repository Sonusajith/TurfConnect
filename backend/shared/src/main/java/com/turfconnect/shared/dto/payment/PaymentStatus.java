package com.turfconnect.shared.dto.payment;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED,
    EXPIRED,
    // Refund lifecycle states (Module 11)
    REFUND_INITIATED,      // refund has been triggered, gateway call pending
    REFUND_PROCESSING,     // gateway acknowledged refund, awaiting settlement
    REFUND_FAILED          // gateway rejected the refund or an error occurred
}

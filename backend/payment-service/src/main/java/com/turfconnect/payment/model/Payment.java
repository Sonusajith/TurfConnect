package com.turfconnect.payment.model;

import com.turfconnect.shared.dto.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String transactionId;

    private String paymentReference;

    private String providerPaymentId;

    private String gatewaySignature;

    private BigDecimal amount;
    
    private String currency;

    private String provider; // STRIPE, RAZORPAY, MOCK

    @Indexed
    private PaymentStatus status;

    @Indexed(unique = true, sparse = true)
    private String idempotencyKey;

    private String failureReason;

    private Map<String, Object> gatewayResponse;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    // === Refund sub-document (Module 11) ===
    // Embedded here to keep refund lifecycle atomically tied to the payment.
    // Null until a refund is initiated.
    private Refund refund;

    // Timestamp when refund was first initiated (for SLA tracking)
    private LocalDateTime refundInitiatedAt;
}

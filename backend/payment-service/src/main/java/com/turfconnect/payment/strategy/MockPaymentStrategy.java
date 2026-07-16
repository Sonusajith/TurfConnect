package com.turfconnect.payment.strategy;

import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component("MOCK")
public class MockPaymentStrategy implements PaymentGatewayStrategy {

    @Override
    public PaymentResponse initiate(PaymentInitiateRequest request) {
        String txId = "mock_tx_" + UUID.randomUUID().toString();
        String refId = "mock_ref_" + UUID.randomUUID().toString();

        return PaymentResponse.builder()
                .bookingId(request.getBookingId())
                .transactionId(txId)
                .paymentReference(refId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .provider("MOCK")
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        // Validate signature contains "mock_sig"
        return signatureHeader != null && signatureHeader.startsWith("mock_signature_");
    }

    @Override
    public PaymentStatus processWebhookEvent(String payload) {
        if (payload != null && payload.contains("success")) {
            return PaymentStatus.SUCCESS;
        } else if (payload != null && payload.contains("fail")) {
            return PaymentStatus.FAILED;
        }
        return PaymentStatus.PENDING;
    }
}

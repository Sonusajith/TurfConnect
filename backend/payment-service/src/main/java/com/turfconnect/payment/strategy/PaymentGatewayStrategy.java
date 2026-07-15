package com.turfconnect.payment.strategy;

import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;

public interface PaymentGatewayStrategy {
    
    /**
     * Initiates a payment with the provider.
     * 
     * @param request the request containing details of the payment
     * @return the response containing gateway transaction references and status
     */
    PaymentResponse initiate(PaymentInitiateRequest request);

    /**
     * Verifies the signature of the webhook payload.
     * 
     * @param payload the raw webhook body payload
     * @param signatureHeader the signature header sent by the provider
     * @return true if the signature is valid, false otherwise
     */
    boolean verifyWebhookSignature(String payload, String signatureHeader);

    /**
     * Parses the webhook payload to determine the payment status.
     * 
     * @param payload the raw webhook body payload
     * @return the resolved PaymentStatus outcome
     */
    PaymentStatus processWebhookEvent(String payload);
}

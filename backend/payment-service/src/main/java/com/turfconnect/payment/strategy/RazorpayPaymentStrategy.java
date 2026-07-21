package com.turfconnect.payment.strategy;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component("RAZORPAY")
@Slf4j
public class RazorpayPaymentStrategy implements PaymentGatewayStrategy {

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentResponse initiate(PaymentInitiateRequest request) {
        if (keyId == null || keyId.trim().isEmpty() || keyId.contains("your_razorpay")) {
            throw new BadRequestException("Razorpay gateway is not configured. Please supply a valid Key ID.");
        }
        if (keySecret == null || keySecret.trim().isEmpty() || keySecret.contains("your_razorpay")) {
            throw new BadRequestException("Razorpay gateway is not configured. Please supply a valid Key Secret.");
        }

        // Razorpay expects amount in paise, so Rs. 10.00 is sent as 1000.
        long amountPaise = request.getAmount().multiply(new BigDecimal("100")).longValue();
        String orderId;

        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaise);
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getBookingId());

            Order order = razorpay.orders.create(orderRequest);
            orderId = order.get("id");
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay Order", e);
            throw new RuntimeException("Payment initiation failed with Razorpay: " + e.getMessage());
        }

        return PaymentResponse.builder()
                .bookingId(request.getBookingId())
                .transactionId(orderId)
                .paymentReference(orderId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .provider("RAZORPAY")
                .status(PaymentStatus.PENDING)
                .orderId(orderId)
                .keyId(keyId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            log.warn("Razorpay webhook secret is empty. Signature verification skipped (unsafe).");
            return true;
        }
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes("UTF-8"), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String expectedSignature = hexString.toString();
            return expectedSignature.equals(signatureHeader);
        } catch (Exception e) {
            log.error("Razorpay webhook signature calculation failed", e);
            return false;
        }
    }

    @Override
    public PaymentStatus processWebhookEvent(String payload) {
        if (payload != null && payload.contains("order.paid")) {
            return PaymentStatus.SUCCESS;
        } else if (payload != null && payload.contains("payment.failed")) {
            return PaymentStatus.FAILED;
        }
        return PaymentStatus.PROCESSING;
    }
}

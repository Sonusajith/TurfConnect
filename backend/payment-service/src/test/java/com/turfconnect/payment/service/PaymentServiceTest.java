package com.turfconnect.payment.service;

import com.turfconnect.payment.model.Payment;
import com.turfconnect.payment.repository.PaymentRepository;
import com.turfconnect.payment.strategy.PaymentGatewayStrategy;
import com.turfconnect.shared.dto.payment.PaymentInitiateRequest;
import com.turfconnect.shared.dto.payment.PaymentResponse;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayStrategy stripeStrategy;

    @Mock
    private PaymentGatewayStrategy razorpayStrategy;

    @Mock
    private PaymentGatewayStrategy mockStrategy;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        Map<String, PaymentGatewayStrategy> strategyMap = new HashMap<>();
        strategyMap.put("STRIPE", stripeStrategy);
        strategyMap.put("RAZORPAY", razorpayStrategy);
        strategyMap.put("MOCK", mockStrategy);

        paymentService = new PaymentService(paymentRepository, strategyMap, restTemplate, rabbitTemplate);
        ReflectionTestUtils.setField(paymentService, "bookingServiceUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(paymentService, "internalTokenSecret", "secret-test-token");
    }

    @Test
    void initiatePayment_IdempotentRequest_ReturnsExistingRecord() {
        String key = "idemp-111";
        Payment existing = Payment.builder()
                .id("pay-999")
                .bookingId("book-123")
                .transactionId("tx-123")
                .amount(BigDecimal.TEN)
                .provider("MOCK")
                .status(PaymentStatus.PENDING)
                .idempotencyKey(key)
                .build();

        when(paymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId("book-123")
                .amount(BigDecimal.TEN)
                .currency("INR")
                .provider("MOCK")
                .build();

        PaymentResponse response = paymentService.initiatePayment(request, key);

        assertNotNull(response);
        assertEquals("pay-999", response.getId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void initiatePayment_NewRequest_ExecutesStrategyAndSaves() {
        String key = "idemp-222";
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId("book-456")
                .amount(BigDecimal.TEN)
                .currency("USD")
                .provider("MOCK")
                .build();

        PaymentResponse mockResponse = PaymentResponse.builder()
                .bookingId("book-456")
                .transactionId("mock_tx_1")
                .paymentReference("mock_ref_1")
                .amount(BigDecimal.TEN)
                .currency("USD")
                .provider("MOCK")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(mockStrategy.initiate(any(PaymentInitiateRequest.class))).thenReturn(mockResponse);
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId("pay-new");
            return p;
        });

        PaymentResponse response = paymentService.initiatePayment(request, key);

        assertNotNull(response);
        assertEquals("pay-new", response.getId());
        assertEquals("mock_tx_1", response.getTransactionId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void processWebhook_InvalidSignature_ThrowsBadRequestException() {
        when(stripeStrategy.verifyWebhookSignature(anyString(), anyString())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> 
                paymentService.processWebhook("STRIPE", "{}", "invalid_sig")
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processWebhook_DuplicateDelivery_DoesNotUpdateOrCallRest() {
        String payload = "{\"data\":{\"object\":{\"id\":\"tx-dup\"}},\"type\":\"payment_intent.succeeded\"}";
        
        Payment existing = Payment.builder()
                .id("pay-dup")
                .bookingId("book-dup")
                .transactionId("tx-dup")
                .status(PaymentStatus.SUCCESS) // Already success!
                .build();

        when(stripeStrategy.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        when(stripeStrategy.processWebhookEvent(anyString())).thenReturn(PaymentStatus.SUCCESS);
        when(paymentRepository.findByTransactionId("tx-dup")).thenReturn(Optional.of(existing));

        paymentService.processWebhook("STRIPE", payload, "valid_sig");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void verifyPayment_BookingServiceOffline_ThrowsException() {
        Payment pending = Payment.builder()
                .id("pay-pending")
                .bookingId("book-offline")
                .transactionId("tx-offline")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByTransactionId("tx-offline")).thenReturn(Optional.of(pending));
        
        // Mock save returning updated payment
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock RestTemplate calling booking service and throwing exception
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("Booking service is offline"));

        assertThrows(BadRequestException.class, () -> 
                paymentService.verifyPayment("tx-offline")
        );
    }
}

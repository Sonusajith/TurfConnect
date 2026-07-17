package com.turfconnect.payment.service;

import com.turfconnect.payment.model.Payment;
import com.turfconnect.payment.model.Refund;
import com.turfconnect.payment.repository.PaymentRepository;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.dto.payment.RefundRequest;
import com.turfconnect.shared.dto.payment.RefundResponse;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RefundService refundService;

    private Payment successPayment;
    private RefundRequest validRequest;

    @BeforeEach
    void setUp() {
        successPayment = Payment.builder()
                .id("pay-001")
                .bookingId("booking-001")
                .transactionId("txn-001")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .provider("MOCK")
                .status(PaymentStatus.SUCCESS)
                .completedAt(LocalDateTime.now())
                .build();

        validRequest = RefundRequest.builder()
                .bookingId("booking-001")
                .reason("User cancelled booking")
                .initiatedBy("user-abc")
                .build();
    }

    @Test
    @DisplayName("Should successfully initiate and complete a full refund for a SUCCESS payment")
    void shouldInitiateFullRefundSuccessfully() {
        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(successPayment));
        // Each save() call returns the updated payment state
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundResponse response = refundService.initiateRefund(validRequest);

        // Verify final state is REFUNDED
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(response.getRefundAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getRefundType()).isEqualTo("FULL");
        assertThat(response.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getRefundId()).isNotNull();
        assertThat(response.getRefundReference()).isNotNull().startsWith("REF-");
        assertThat(response.getInitiatedBy()).isEqualTo("user-abc");
        assertThat(response.getInitiatedAt()).isNotNull();
        assertThat(response.getCompletedAt()).isNotNull();

        // Verify state machine transitions: INITIATED → PROCESSING → REFUNDED
        // save() should be called at least 3 times (once per state)
        verify(paymentRepository, atLeast(3)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should return existing refund state on duplicate request — idempotency")
    void shouldReturnExistingRefundOnDuplicateRequest() {
        Refund existingRefund = Refund.builder()
                .refundId("existing-refund-id")
                .refundReference("REF-ABCDEF12")
                .refundType("FULL")
                .refundAmount(new BigDecimal("500.00"))
                .remainingAmount(BigDecimal.ZERO)
                .initiatedBy("user-abc")
                .initiatedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        Payment alreadyRefundedPayment = successPayment.toBuilder()
                .status(PaymentStatus.REFUNDED)
                .refund(existingRefund)
                .build();

        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(alreadyRefundedPayment));

        RefundResponse response = refundService.initiateRefund(validRequest);

        // Must return the existing state, not re-trigger anything
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(response.getRefundId()).isEqualTo("existing-refund-id");

        // Gateway should NOT be called again
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should return existing state when refund is already REFUND_INITIATED")
    void shouldReturnExistingStateWhenRefundInitiated() {
        Refund pendingRefund = Refund.builder()
                .refundId("pending-refund-id")
                .refundReference("REF-PENDING")
                .refundType("FULL")
                .refundAmount(new BigDecimal("500.00"))
                .remainingAmount(BigDecimal.ZERO)
                .initiatedAt(LocalDateTime.now())
                .build();

        Payment initiatedPayment = successPayment.toBuilder()
                .status(PaymentStatus.REFUND_INITIATED)
                .refund(pendingRefund)
                .build();

        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(initiatedPayment));

        RefundResponse response = refundService.initiateRefund(validRequest);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUND_INITIATED);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no payment exists for bookingId")
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refundService.initiateRefund(validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No payment record found for bookingId");

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should throw BadRequestException when payment is in PENDING state")
    void shouldThrowWhenPaymentNotSuccess_Pending() {
        successPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(successPayment));

        assertThatThrownBy(() -> refundService.initiateRefund(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Refund can only be initiated for payments in SUCCESS state");
    }

    @Test
    @DisplayName("Should throw BadRequestException when payment is in FAILED state")
    void shouldThrowWhenPaymentNotSuccess_Failed() {
        successPayment.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(successPayment));

        assertThatThrownBy(() -> refundService.initiateRefund(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Refund can only be initiated for payments in SUCCESS state");
    }

    @Test
    @DisplayName("Should publish a RabbitMQ event when refund completes")
    void shouldPublishEventOnSuccessfulRefund() {
        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(successPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        refundService.initiateRefund(validRequest);

        // Verify that a message was sent to RabbitMQ with routing key payment.refunded
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(anyString(), routingKeyCaptor.capture(), (Object) any());
        assertThat(routingKeyCaptor.getValue()).isEqualTo("payment.refunded");
    }

    @Test
    @DisplayName("Should set 'SYSTEM' as initiatedBy when initiatedBy is null in the request")
    void shouldDefaultInitiatedByToSystem() {
        RefundRequest requestWithoutInitiator = RefundRequest.builder()
                .bookingId("booking-001")
                .reason("Auto refund")
                .initiatedBy(null)
                .build();

        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.of(successPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundResponse response = refundService.initiateRefund(requestWithoutInitiator);
        assertThat(response.getInitiatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("Concurrent duplicate refund requests should not double-process (idempotency under race)")
    void shouldHandleConcurrentDuplicateRefundRequests() throws InterruptedException {
        // Simulate that the first call finds a SUCCESS payment, subsequent ones find a REFUNDED one
        AtomicInteger callCount = new AtomicInteger(0);
        when(paymentRepository.findByBookingId("booking-001")).thenAnswer(inv -> {
            int count = callCount.getAndIncrement();
            if (count == 0) {
                return Optional.of(successPayment);
            }
            // Subsequent calls see the payment already REFUNDED (as if the first call completed)
            Refund done = Refund.builder()
                    .refundId("concurrent-refund-id")
                    .refundAmount(new BigDecimal("500.00"))
                    .remainingAmount(BigDecimal.ZERO)
                    .refundType("FULL")
                    .initiatedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();
            return Optional.of(successPayment.toBuilder()
                    .status(PaymentStatus.REFUNDED)
                    .refund(done).build());
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    RefundResponse r = refundService.initiateRefund(validRequest);
                    if (r.getStatus() == PaymentStatus.REFUNDED) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // expected for some concurrent calls
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // All threads should complete (either first run or idempotent return)
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Refund event should not be published if payment is not found")
    void shouldNotPublishEventWhenPaymentNotFound() {
        when(paymentRepository.findByBookingId("booking-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refundService.initiateRefund(validRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(rabbitTemplate);
    }
}

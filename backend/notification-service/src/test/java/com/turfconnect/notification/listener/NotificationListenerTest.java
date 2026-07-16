package com.turfconnect.notification.listener;

import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.event.PaymentEvent;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class NotificationListenerTest {

    @InjectMocks
    private NotificationListener notificationListener;

    private BookingEvent bookingCreatedEvent;
    private BookingEvent bookingConfirmedEvent;
    private BookingEvent bookingCancelledEvent;
    private PaymentEvent paymentSuccessEvent;
    private PaymentEvent paymentFailedEvent;

    @BeforeEach
    void setUp() {
        bookingCreatedEvent = BookingEvent.builder()
                .bookingId("b-1")
                .userId("u-1")
                .turfId("t-1")
                .turfName("Elite Football Arena")
                .date(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .totalPrice(new BigDecimal("1500.00"))
                .status(com.turfconnect.shared.dto.booking.BookingStatus.PENDING)
                .eventType("CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        bookingConfirmedEvent = BookingEvent.builder()
                .bookingId("b-1")
                .userId("u-1")
                .turfId("t-1")
                .turfName("Elite Football Arena")
                .date(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .totalPrice(new BigDecimal("1500.00"))
                .status(com.turfconnect.shared.dto.booking.BookingStatus.CONFIRMED)
                .eventType("CONFIRMED")
                .timestamp(LocalDateTime.now())
                .build();

        bookingCancelledEvent = BookingEvent.builder()
                .bookingId("b-1")
                .userId("u-1")
                .turfId("t-1")
                .turfName("Elite Football Arena")
                .date(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .totalPrice(new BigDecimal("1500.00"))
                .status(com.turfconnect.shared.dto.booking.BookingStatus.CANCELLED)
                .eventType("CANCELLED")
                .timestamp(LocalDateTime.now())
                .build();

        paymentSuccessEvent = PaymentEvent.builder()
                .transactionId("tx-1")
                .bookingId("b-1")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .eventType("SUCCESS")
                .timestamp(LocalDateTime.now())
                .build();

        paymentFailedEvent = PaymentEvent.builder()
                .transactionId("tx-2")
                .bookingId("b-1")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.FAILED)
                .eventType("FAILED")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void handleBookingEvent_Created_DoesNotThrow() {
        assertDoesNotThrow(() -> notificationListener.handleBookingEvent(bookingCreatedEvent));
    }

    @Test
    void handleBookingEvent_Confirmed_DoesNotThrow() {
        assertDoesNotThrow(() -> notificationListener.handleBookingEvent(bookingConfirmedEvent));
    }

    @Test
    void handleBookingEvent_Cancelled_DoesNotThrow() {
        assertDoesNotThrow(() -> notificationListener.handleBookingEvent(bookingCancelledEvent));
    }

    @Test
    void handlePaymentEvent_Success_DoesNotThrow() {
        assertDoesNotThrow(() -> notificationListener.handlePaymentEvent(paymentSuccessEvent));
    }

    @Test
    void handlePaymentEvent_Failed_DoesNotThrow() {
        assertDoesNotThrow(() -> notificationListener.handlePaymentEvent(paymentFailedEvent));
    }
}

package com.turfconnect.fraud.listener;

import com.turfconnect.fraud.config.RabbitMQConfig;
import com.turfconnect.fraud.service.FraudDetectionService;
import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.booking.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final FraudDetectionService fraudDetectionService;

    @RabbitListener(queues = RabbitMQConfig.FRAUD_BOOKING_QUEUE)
    public void handleBookingEvent(BookingEvent event) {
        log.debug("Received booking event for user: {}, status: {}", event.getUserId(), event.getStatus());
        
        try {
            if (event.getStatus() == BookingStatus.PENDING || "CREATED".equals(event.getEventType())) {
                // A new booking attempt
                fraudDetectionService.recordBookingAttempt(event.getUserId());
            } else if (event.getStatus() == BookingStatus.CANCELLED) {
                // A booking cancellation
                fraudDetectionService.recordCancellation(event.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to process fraud booking event for user {}: {}", event.getUserId(), e.getMessage());
            throw e; // Throw exception to trigger DLQ retry
        }
    }
}

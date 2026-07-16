package com.turfconnect.notification.listener;

import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes events from RabbitMQ and logs simulated notifications.
 */
@Component
@Slf4j
public class NotificationListener {

    /**
     * Listen to booking event queues and dispatch alert simulations.
     */
    @RabbitListener(queues = "booking.notification.queue")
    public void handleBookingEvent(BookingEvent event) {
        log.info("🔔 [EVENT CONSUMED] Received BookingEvent: ID={}, Type={}, Status={}",
                event.getBookingId(), event.getEventType(), event.getStatus());

        String eventType = event.getEventType().toUpperCase();
        switch (eventType) {
            case "CREATED":
                sendMockNotification(event.getUserId(),
                        "Booking Created! Your slot at " + event.getTurfName() + " on " +
                        event.getDate() + " (" + event.getStartTime() + " - " + event.getEndTime() + ") is pending checkout. Total Price: INR " + event.getTotalPrice());
                break;
            case "CONFIRMED":
                sendMockNotification(event.getUserId(),
                        "Booking Confirmed! You are ready to play at " + event.getTurfName() + " on " +
                        event.getDate() + " (" + event.getStartTime() + " - " + event.getEndTime() + "). See you there!");
                break;
            case "CANCELLED":
                sendMockNotification(event.getUserId(),
                        "Booking Cancelled. Your slot booking at " + event.getTurfName() + " on " +
                        event.getDate() + " has been successfully released/cancelled.");
                break;
            default:
                log.warn("Unknown booking event type received: {}", eventType);
        }
    }

    /**
     * Listen to payment event queues and dispatch alert simulations.
     */
    @RabbitListener(queues = "payment.notification.queue")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("🔔 [EVENT CONSUMED] Received PaymentEvent: TxId={}, Type={}, Status={}",
                event.getTransactionId(), event.getEventType(), event.getStatus());

        String eventType = event.getEventType().toUpperCase();
        switch (eventType) {
            case "SUCCESS":
                sendMockNotification("system_admin",
                        "Payment Successful! Transaction ID " + event.getTransactionId() + " for Booking ID " +
                        event.getBookingId() + " of amount INR " + event.getAmount() + " completed successfully.");
                break;
            case "FAILED":
                sendMockNotification("system_admin",
                        "Payment Failed. Transaction ID " + event.getTransactionId() + " for Booking ID " +
                        event.getBookingId() + " of amount INR " + event.getAmount() + " failed.");
                break;
            default:
                log.warn("Unknown payment event type received: {}", eventType);
        }
    }

    private void sendMockNotification(String recipientId, String message) {
        // Logging simulates SMS, email, or push notifications dispatch
        log.info("✉️ [NOTIFICATION DISPATCHED] To Recipient: {} | Message: {}", recipientId, message);
    }
}

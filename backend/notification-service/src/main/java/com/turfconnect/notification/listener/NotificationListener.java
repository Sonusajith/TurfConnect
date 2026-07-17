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
            // Module 11 — Refund events
            case "REFUNDED":
                String refundAmt = event.getRefundAmount() != null ? event.getRefundAmount().toPlainString() : "N/A";
                String refundRef = event.getRefundReference() != null ? event.getRefundReference() : "N/A";
                sendMockNotification("system_admin",
                        "Refund Processed! ₹" + refundAmt + " refunded for Booking ID " + event.getBookingId() +
                        ". Refund Reference: " + refundRef + ". Funds will appear within 5-7 business days.");
                break;
            case "REFUND_FAILED":
                sendMockNotification("system_admin",
                        "Refund Failed. The refund for Booking ID " + event.getBookingId() +
                        " could not be processed. Please contact support with reference: " +
                        event.getTransactionId());
                break;
            default:
                log.warn("Unknown payment event type received: {}", eventType);
        }
    }

    private void sendMockNotification(String recipientId, String message) {
        // Logging simulates SMS, email, or push notifications dispatch
        log.info("✉️ [NOTIFICATION DISPATCHED] To Recipient: {} | Message: {}", recipientId, message);
    }

    /**
     * Listen to community invitation events and dispatch email notifications.
     * Module 13 — Teams & Invitations
     */
    @RabbitListener(queues = "community.notification.queue")
    public void handleTeamInvitationEvent(com.turfconnect.shared.dto.event.TeamInvitationNotificationEvent event) {
        log.info("🔔 [EVENT CONSUMED] Received TeamInvitationEvent: InvitationId={}, TeamId={}, InviteeEmail={}",
                event.getInvitationId(), event.getTeamId(), event.getInviteeEmail());

        sendMockNotification(event.getInviteeEmail(),
                "You have been invited to join team '" + event.getTeamName() + "' by " + event.getInviterName() +
                (event.getMessage() != null ? ". Message: " + event.getMessage() : "") +
                ". This invitation expires at: " + event.getExpiresAt() + ". Login to accept or decline.");
    }

    /**
     * Listen to community match events and dispatch mock notifications to captains.
     * Module 14 — Matches
     */
    @RabbitListener(queues = "community.match.notification.queue")
    public void handleMatchNotificationEvent(com.turfconnect.shared.dto.event.MatchNotificationEvent event) {
        log.info("🔔 [EVENT CONSUMED] Received MatchNotificationEvent: MatchId={}, EventType={}, HomeTeam={}, AwayTeam={}",
                event.getMatchId(), event.getEventType(), event.getHomeTeamName(), event.getAwayTeamName());

        String message = String.format("Match Update: Your team is involved in a match %s on %s at %s.", 
                event.getEventType(), event.getDate(), event.getStartTime());
                
        // In reality, this would look up the captain emails for both teams
        sendMockNotification("team-captains-of-match-" + event.getMatchId(), message);
    }
}

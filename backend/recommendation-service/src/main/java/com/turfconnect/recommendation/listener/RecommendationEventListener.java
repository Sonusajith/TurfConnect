package com.turfconnect.recommendation.listener;

import com.turfconnect.recommendation.config.RabbitMQConfig;
import com.turfconnect.recommendation.service.RecommendationService;
import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.event.ReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationEventListener {

    private final RecommendationService recommendationService;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void handleBookingEvent(BookingEvent event) {
        log.info("Received booking event for turf: {}, status: {}", event.getTurfId(), event.getStatus());
        try {
            recommendationService.processBookingEvent(event.getTurfId(), event.getStatus() != null ? event.getStatus().name() : "UNKNOWN");
        } catch (Exception e) {
            log.error("Failed to process booking event: {}", e.getMessage());
            throw e; // Throwing exception triggers RabbitMQ retry and eventual DLQ
        }
    }

    @RabbitListener(queues = RabbitMQConfig.REVIEW_QUEUE)
    public void handleReviewEvent(ReviewEvent event) {
        log.info("Received review event for turf: {}, rating: {}", event.getTurfId(), event.getAverageRating());
        try {
            // Assume the event sends the individual rating, or the review-service sends the new average.
            // For this heuristic, we pass the rating down.
            recommendationService.processReviewEvent(event.getTurfId(), event.getAverageRating() != null ? event.getAverageRating() : 0.0);
        } catch (Exception e) {
            log.error("Failed to process review event: {}", e.getMessage());
            throw e; // Throwing exception triggers RabbitMQ retry and eventual DLQ
        }
    }
}

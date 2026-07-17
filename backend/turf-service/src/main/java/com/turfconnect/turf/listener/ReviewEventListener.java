package com.turfconnect.turf.listener;

import com.turfconnect.shared.dto.event.ReviewEvent;
import com.turfconnect.turf.service.TurfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener that consumes review events and updates cached/stored ratings in turf-service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventListener {

    private final TurfService turfService;

    @RabbitListener(queues = com.turfconnect.turf.config.RabbitMQConfig.TURF_REVIEW_QUEUE)
    public void handleReviewEvent(ReviewEvent event) {
        log.info("🔔 [EVENT CONSUMED] Received ReviewEvent for turfId: {}, averageRating: {}, totalReviews: {}",
                event.getTurfId(), event.getAverageRating(), event.getTotalReviews());
        try {
            turfService.updateTurfRating(event.getTurfId(), event.getAverageRating());
            log.info("Successfully updated averageRating for turfId: {} to {}", event.getTurfId(), event.getAverageRating());
        } catch (Exception e) {
            log.error("Failed to process review event for turfId: " + event.getTurfId(), e);
            throw e; // triggers retry mechanism / dead letter queue redirection
        }
    }
}

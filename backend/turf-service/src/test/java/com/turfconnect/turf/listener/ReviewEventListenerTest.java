package com.turfconnect.turf.listener;

import com.turfconnect.shared.dto.event.ReviewEvent;
import com.turfconnect.turf.service.TurfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewEventListenerTest {

    @Mock
    private TurfService turfService;

    @InjectMocks
    private ReviewEventListener reviewEventListener;

    @Test
    void handleReviewEvent_Success_DelegatesToTurfService() {
        ReviewEvent event = ReviewEvent.builder()
                .eventId("e-1")
                .eventType("REVIEW_UPDATED")
                .version("1.0")
                .timestamp(LocalDateTime.now())
                .turfId("t-1")
                .averageRating(4.8)
                .totalReviews(5)
                .build();

        doNothing().when(turfService).updateTurfRating("t-1", 4.8);

        reviewEventListener.handleReviewEvent(event);

        verify(turfService, times(1)).updateTurfRating("t-1", 4.8);
    }

    @Test
    void handleReviewEvent_ExceptionInService_PropagatesException() {
        ReviewEvent event = ReviewEvent.builder()
                .eventId("e-2")
                .eventType("REVIEW_UPDATED")
                .version("1.0")
                .timestamp(LocalDateTime.now())
                .turfId("t-2")
                .averageRating(3.5)
                .totalReviews(2)
                .build();

        doThrow(new RuntimeException("Database error")).when(turfService).updateTurfRating("t-2", 3.5);

        assertThrows(RuntimeException.class, () ->
                reviewEventListener.handleReviewEvent(event)
        );

        verify(turfService, times(1)).updateTurfRating("t-2", 3.5);
    }
}

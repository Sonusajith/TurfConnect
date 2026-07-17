package com.turfconnect.recommendation.listener;

import com.turfconnect.recommendation.service.RecommendationService;
import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.event.ReviewEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationEventListener Tests")
class RecommendationEventListenerTest {

    @Mock private RecommendationService recommendationService;
    @InjectMocks private RecommendationEventListener listener;

    @Test
    @DisplayName("handleBookingEvent: calls service")
    void handleBookingEvent_callsService() {
        BookingEvent event = new BookingEvent();
        event.setTurfId("turf-1");
        event.setStatus(com.turfconnect.shared.dto.booking.BookingStatus.CONFIRMED);

        listener.handleBookingEvent(event);

        verify(recommendationService).processBookingEvent("turf-1", "CONFIRMED");
    }

    @Test
    @DisplayName("handleBookingEvent: throws on error to trigger DLQ")
    void handleBookingEvent_throwsOnError() {
        BookingEvent event = new BookingEvent();
        event.setTurfId("turf-1");
        
        doThrow(new RuntimeException("DB down")).when(recommendationService).processBookingEvent(any(), any());

        assertThatThrownBy(() -> listener.handleBookingEvent(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB down");
    }
}

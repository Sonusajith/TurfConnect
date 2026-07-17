package com.turfconnect.analytics.service;

import com.turfconnect.analytics.model.PlatformDailyMetrics;
import com.turfconnect.shared.dto.event.BookingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import(AnalyticsAggregationService.class)
class AnalyticsAggregationServiceTest {

    @Autowired
    private AnalyticsAggregationService aggregationService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(PlatformDailyMetrics.class);
    }

    @Test
    void testProcessBookingEvent_CreatesMetrics_AndIsIdempotent() {
        BookingEvent event = BookingEvent.builder()
                .bookingId("b1")
                .eventType("CREATED")
                .turfId("turf1")
                .build();

        // First processing
        aggregationService.processBookingEvent(event);

        List<PlatformDailyMetrics> metrics = mongoTemplate.findAll(PlatformDailyMetrics.class);
        assertEquals(1, metrics.size());
        assertEquals(1, metrics.get(0).getTotalBookings());
        assertEquals(1, metrics.get(0).getProcessedEventIds().size());
        assertTrue(metrics.get(0).getProcessedEventIds().contains("b1-CREATED"));

        // Second processing (Idempotency check)
        aggregationService.processBookingEvent(event);

        metrics = mongoTemplate.findAll(PlatformDailyMetrics.class);
        assertEquals(1, metrics.size());
        // Should still be 1 total booking
        assertEquals(1, metrics.get(0).getTotalBookings());
        assertEquals(1, metrics.get(0).getProcessedEventIds().size());
    }

    @Test
    void testProcessBookingEvent_ConfirmedAndCancelled_UpdatesCorrectly() {
        // CREATED
        BookingEvent created = BookingEvent.builder()
                .bookingId("b2")
                .eventType("CREATED")
                .build();
        aggregationService.processBookingEvent(created);

        // CONFIRMED
        BookingEvent confirmed = BookingEvent.builder()
                .bookingId("b2")
                .eventType("CONFIRMED")
                .totalPrice(new BigDecimal("100.50"))
                .build();
        aggregationService.processBookingEvent(confirmed);

        PlatformDailyMetrics metrics = mongoTemplate.findAll(PlatformDailyMetrics.class).get(0);
        assertEquals(1, metrics.getTotalBookings());
        assertEquals(1, metrics.getConfirmedBookings());
        assertEquals(0, metrics.getCancelledBookings());
        assertEquals(100.50, metrics.getTotalRevenue());

        // CANCELLED
        BookingEvent cancelled = BookingEvent.builder()
                .bookingId("b2")
                .eventType("CANCELLED")
                .totalPrice(new BigDecimal("100.50"))
                .build();
        aggregationService.processBookingEvent(cancelled);

        metrics = mongoTemplate.findAll(PlatformDailyMetrics.class).get(0);
        assertEquals(1, metrics.getTotalBookings());
        assertEquals(1, metrics.getConfirmedBookings());
        assertEquals(1, metrics.getCancelledBookings());
        assertEquals(0.0, metrics.getTotalRevenue());
    }
}

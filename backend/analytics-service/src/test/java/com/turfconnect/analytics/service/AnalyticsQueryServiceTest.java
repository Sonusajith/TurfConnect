package com.turfconnect.analytics.service;

import com.turfconnect.analytics.dto.AnalyticsSummaryResponse;
import com.turfconnect.analytics.model.PlatformDailyMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import(AnalyticsQueryService.class)
class AnalyticsQueryServiceTest {

    @Autowired
    private AnalyticsQueryService queryService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(PlatformDailyMetrics.class);
    }

    @Test
    void testGetPlatformSummary_CalculatesDerivedMetricsCorrectly() {
        PlatformDailyMetrics day1 = new PlatformDailyMetrics();
        day1.setDate(LocalDate.now().minusDays(1));
        day1.setTotalBookings(10);
        day1.setConfirmedBookings(8);
        day1.setCancelledBookings(2);
        day1.setTotalRevenue(800.0);
        day1.setFraudAlerts(1);
        mongoTemplate.save(day1);

        PlatformDailyMetrics day2 = new PlatformDailyMetrics();
        day2.setDate(LocalDate.now());
        day2.setTotalBookings(5);
        day2.setConfirmedBookings(5);
        day2.setCancelledBookings(0);
        day2.setTotalRevenue(500.0);
        day2.setFraudAlerts(0);
        mongoTemplate.save(day2);

        AnalyticsSummaryResponse summary = queryService.getPlatformSummary(
                LocalDate.now().minusDays(5), LocalDate.now());

        assertEquals(15, summary.getTotalBookings());
        assertEquals(13, summary.getConfirmedBookings());
        assertEquals(2, summary.getCancelledBookings());
        assertEquals(1300.0, summary.getTotalRevenue());
        assertEquals(1, summary.getFraudAlerts());

        // Derived metrics
        assertEquals(13.0 / 15.0 * 100, summary.getConfirmationRate(), 0.01);
        assertEquals(2.0 / 15.0 * 100, summary.getCancellationRate(), 0.01);
        assertEquals(1300.0 / 13.0, summary.getAverageRevenuePerBooking(), 0.01);
    }
}

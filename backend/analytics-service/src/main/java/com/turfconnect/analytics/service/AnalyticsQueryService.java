package com.turfconnect.analytics.service;

import com.turfconnect.analytics.dto.AnalyticsSummaryResponse;
import com.turfconnect.analytics.model.PlatformDailyMetrics;
import com.turfconnect.analytics.model.TurfDailyMetrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsQueryService {

    private final MongoTemplate mongoTemplate;

    public AnalyticsQueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public AnalyticsSummaryResponse getPlatformSummary(LocalDate startDate, LocalDate endDate) {
        Query query = new Query(Criteria.where("date").gte(startDate).lte(endDate));
        List<PlatformDailyMetrics> metricsList = mongoTemplate.find(query, PlatformDailyMetrics.class);

        long totalBookings = 0;
        long confirmedBookings = 0;
        long cancelledBookings = 0;
        double totalRevenue = 0;
        long fraudAlerts = 0;

        for (PlatformDailyMetrics metrics : metricsList) {
            totalBookings += metrics.getTotalBookings();
            confirmedBookings += metrics.getConfirmedBookings();
            cancelledBookings += metrics.getCancelledBookings();
            totalRevenue += metrics.getTotalRevenue();
            fraudAlerts += metrics.getFraudAlerts();
        }

        return buildResponse(totalBookings, confirmedBookings, cancelledBookings, totalRevenue, fraudAlerts);
    }

    public AnalyticsSummaryResponse getTurfSummary(String turfId, LocalDate startDate, LocalDate endDate) {
        Query query = new Query(Criteria.where("turfId").is(turfId).and("date").gte(startDate).lte(endDate));
        List<TurfDailyMetrics> metricsList = mongoTemplate.find(query, TurfDailyMetrics.class);

        long totalBookings = 0;
        long confirmedBookings = 0;
        long cancelledBookings = 0;
        double totalRevenue = 0;

        for (TurfDailyMetrics metrics : metricsList) {
            totalBookings += metrics.getTotalBookings();
            confirmedBookings += metrics.getConfirmedBookings();
            cancelledBookings += metrics.getCancelledBookings();
            totalRevenue += metrics.getTotalRevenue();
        }

        return buildResponse(totalBookings, confirmedBookings, cancelledBookings, totalRevenue, 0);
    }

    private AnalyticsSummaryResponse buildResponse(long total, long confirmed, long cancelled, double revenue, long fraudAlerts) {
        double confirmationRate = total > 0 ? (double) confirmed / total * 100 : 0.0;
        double cancellationRate = total > 0 ? (double) cancelled / total * 100 : 0.0;
        double averageRevenue = confirmed > 0 ? revenue / confirmed : 0.0;

        return AnalyticsSummaryResponse.builder()
                .totalBookings(total)
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .totalRevenue(revenue)
                .fraudAlerts(fraudAlerts)
                .confirmationRate(confirmationRate)
                .cancellationRate(cancellationRate)
                .averageRevenuePerBooking(averageRevenue)
                .build();
    }
}

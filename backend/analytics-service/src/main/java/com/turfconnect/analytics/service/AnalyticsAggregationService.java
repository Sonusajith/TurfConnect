package com.turfconnect.analytics.service;

import com.turfconnect.analytics.model.PlatformDailyMetrics;
import com.turfconnect.analytics.model.TurfDailyMetrics;
import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.event.FraudAlertEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AnalyticsAggregationService {

    private final MongoTemplate mongoTemplate;

    public AnalyticsAggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void processBookingEvent(BookingEvent event) {
        String eventId = event.getBookingId() + "-" + event.getEventType();
        LocalDate date = LocalDate.now();

        // Platform Metrics Update
        updatePlatformBookingMetrics(date, event, eventId);

        // Turf Metrics Update
        if (event.getTurfId() != null) {
            updateTurfBookingMetrics(date, event.getTurfId(), event, eventId);
        }
    }

    public void processFraudAlertEvent(FraudAlertEvent event) {
        String eventId = event.getUserId() + "-" + event.getAlertType() + "-" + event.getTimestamp();
        LocalDate date = LocalDate.now();

        Query updateQuery = new Query(Criteria.where("date").is(date)
                .and("processedEventIds").ne(eventId));

        Update update = new Update()
                .inc("fraudAlerts", 1)
                .push("processedEventIds", eventId)
                .set("updatedAt", LocalDateTime.now());

        var result = mongoTemplate.updateFirst(updateQuery, update, PlatformDailyMetrics.class);

        if (result.getMatchedCount() == 0) {
            Query existsQuery = new Query(Criteria.where("date").is(date));
            if (!mongoTemplate.exists(existsQuery, PlatformDailyMetrics.class)) {
                Query upsertQuery = new Query(Criteria.where("date").is(date));
                Update upsertUpdate = new Update()
                        .inc("fraudAlerts", 1)
                        .push("processedEventIds", eventId)
                        .set("updatedAt", LocalDateTime.now())
                        .setOnInsert("createdAt", LocalDateTime.now())
                        .setOnInsert("date", date);
                mongoTemplate.upsert(upsertQuery, upsertUpdate, PlatformDailyMetrics.class);
            }
        }
    }

    private void updatePlatformBookingMetrics(LocalDate date, BookingEvent event, String eventId) {
        Query updateQuery = new Query(Criteria.where("date").is(date)
                .and("processedEventIds").ne(eventId));

        Update update = buildBookingUpdate(event, eventId);

        var result = mongoTemplate.updateFirst(updateQuery, update, PlatformDailyMetrics.class);

        if (result.getMatchedCount() == 0) {
            Query existsQuery = new Query(Criteria.where("date").is(date));
            if (!mongoTemplate.exists(existsQuery, PlatformDailyMetrics.class)) {
                Query upsertQuery = new Query(Criteria.where("date").is(date));
                Update upsertUpdate = buildBookingUpdate(event, eventId).setOnInsert("date", date);
                mongoTemplate.upsert(upsertQuery, upsertUpdate, PlatformDailyMetrics.class);
            }
        }
    }

    private void updateTurfBookingMetrics(LocalDate date, String turfId, BookingEvent event, String eventId) {
        Query updateQuery = new Query(Criteria.where("date").is(date)
                .and("turfId").is(turfId)
                .and("processedEventIds").ne(eventId));

        Update update = buildBookingUpdate(event, eventId);

        var result = mongoTemplate.updateFirst(updateQuery, update, TurfDailyMetrics.class);

        if (result.getMatchedCount() == 0) {
            Query existsQuery = new Query(Criteria.where("date").is(date).and("turfId").is(turfId));
            if (!mongoTemplate.exists(existsQuery, TurfDailyMetrics.class)) {
                Query upsertQuery = new Query(Criteria.where("date").is(date).and("turfId").is(turfId));
                Update upsertUpdate = buildBookingUpdate(event, eventId)
                        .setOnInsert("date", date)
                        .setOnInsert("turfId", turfId);
                mongoTemplate.upsert(upsertQuery, upsertUpdate, TurfDailyMetrics.class);
            }
        }
    }

    private Update buildBookingUpdate(BookingEvent event, String eventId) {
        Update update = new Update()
                .push("processedEventIds", eventId)
                .set("updatedAt", LocalDateTime.now())
                .setOnInsert("createdAt", LocalDateTime.now());

        if ("CREATED".equals(event.getEventType())) {
            update.inc("totalBookings", 1);
        } else if ("CONFIRMED".equals(event.getEventType())) {
            update.inc("confirmedBookings", 1);
            if (event.getTotalPrice() != null) {
                update.inc("totalRevenue", event.getTotalPrice().doubleValue());
            }
        } else if ("CANCELLED".equals(event.getEventType())) {
            update.inc("cancelledBookings", 1);
            if (event.getTotalPrice() != null) {
                // If a confirmed booking is cancelled, we might want to subtract revenue.
                // Assuming it was confirmed before, we decrement revenue.
                update.inc("totalRevenue", -event.getTotalPrice().doubleValue());
            }
        }
        return update;
    }
}

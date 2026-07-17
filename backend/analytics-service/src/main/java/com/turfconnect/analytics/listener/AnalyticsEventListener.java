package com.turfconnect.analytics.listener;

import com.turfconnect.analytics.config.RabbitMQConfig;
import com.turfconnect.analytics.service.AnalyticsAggregationService;
import com.turfconnect.shared.dto.event.BookingEvent;
import com.turfconnect.shared.dto.event.FraudAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);
    private final AnalyticsAggregationService aggregationService;

    public AnalyticsEventListener(AnalyticsAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_BOOKING_QUEUE)
    public void onBookingEvent(BookingEvent event) {
        log.info("Received BookingEvent for analytics: {}", event);
        try {
            aggregationService.processBookingEvent(event);
        } catch (Exception e) {
            log.error("Failed to process BookingEvent for analytics: {}", event, e);
            throw e; // Will trigger retry and eventually DLQ
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_FRAUD_QUEUE)
    public void onFraudAlertEvent(FraudAlertEvent event) {
        log.info("Received FraudAlertEvent for analytics: {}", event);
        try {
            aggregationService.processFraudAlertEvent(event);
        } catch (Exception e) {
            log.error("Failed to process FraudAlertEvent for analytics: {}", event, e);
            throw e; // Will trigger retry and eventually DLQ
        }
    }
}

package com.turfconnect.fraud.service;

import com.turfconnect.fraud.config.FraudProperties;
import com.turfconnect.fraud.config.RabbitMQConfig;
import com.turfconnect.shared.dto.event.FraudAlertEvent;
import com.turfconnect.shared.dto.event.FraudAlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final StringRedisTemplate redisTemplate;
    private final FraudProperties fraudProperties;
    private final RabbitTemplate rabbitTemplate;

    private static final String BOOKING_KEY_PREFIX = "fraud:booking:user:";
    private static final String CANCEL_KEY_PREFIX = "fraud:cancel:user:";
    private static final String FLAG_KEY_PREFIX = "fraud:flag:user:";

    @Override
    public void recordBookingAttempt(String userId) {
        String key = BOOKING_KEY_PREFIX + userId;
        int limit = fraudProperties.getThresholds().getBooking().getLimit();
        int windowHours = fraudProperties.getThresholds().getBooking().getWindowHours();
        
        long count = incrementAndExpire(key, windowHours);
        
        if (count > limit) {
            log.warn("User {} exceeded booking velocity threshold ({} > {}) in {}h", userId, count, limit, windowHours);
            flagUserAndPublishAlert(userId, FraudAlertType.HIGH_BOOKING_VELOCITY, limit, (int) count);
        }
    }

    @Override
    public void recordCancellation(String userId) {
        String key = CANCEL_KEY_PREFIX + userId;
        int limit = fraudProperties.getThresholds().getCancellation().getLimit();
        int windowHours = fraudProperties.getThresholds().getCancellation().getWindowHours();
        
        long count = incrementAndExpire(key, windowHours);
        
        if (count > limit) {
            log.warn("User {} exceeded cancellation velocity threshold ({} > {}) in {}h", userId, count, limit, windowHours);
            flagUserAndPublishAlert(userId, FraudAlertType.EXCESSIVE_CANCELLATIONS, limit, (int) count);
        }
    }

    private long incrementAndExpire(String key, int windowHours) {
        Long count = redisTemplate.opsForValue().increment(key);
        // Atomicity note: This is two operations (INCR then EXPIRE). 
        // We only set the expiry if this is the first increment (count == 1).
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowHours, TimeUnit.HOURS);
        }
        return count != null ? count : 0;
    }

    private void flagUserAndPublishAlert(String userId, FraudAlertType alertType, int threshold, int currentValue) {
        String flagKey = FLAG_KEY_PREFIX + userId + ":" + alertType.name();
        
        // Use setIfAbsent to ensure we only publish the alert once per TTL period, avoiding flood of alerts
        Boolean newlyFlagged = redisTemplate.opsForValue().setIfAbsent(
                flagKey, 
                String.valueOf(currentValue), 
                fraudProperties.getFlag().getTtlDays(), 
                TimeUnit.DAYS
        );

        if (Boolean.TRUE.equals(newlyFlagged)) {
            log.info("Publishing FraudAlertEvent for user {}", userId);
            
            FraudAlertEvent alertEvent = FraudAlertEvent.builder()
                    .userId(userId)
                    .alertType(alertType)
                    .triggeredThreshold(threshold)
                    .currentCounterValue(currentValue)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            rabbitTemplate.convertAndSend(RabbitMQConfig.FRAUD_EXCHANGE, "fraud.alert", alertEvent);
        }
    }
}

package com.turfconnect.fraud.service;

import com.turfconnect.fraud.config.FraudProperties;
import com.turfconnect.fraud.config.RabbitMQConfig;
import com.turfconnect.shared.dto.event.FraudAlertEvent;
import com.turfconnect.shared.dto.event.FraudAlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private FraudDetectionServiceImpl service;

    @BeforeEach
    void setUp() {
        FraudProperties properties = new FraudProperties();
        properties.getThresholds().getBooking().setLimit(5);
        properties.getThresholds().getBooking().setWindowHours(1);
        properties.getThresholds().getCancellation().setLimit(3);
        properties.getThresholds().getCancellation().setWindowHours(24);
        properties.getFlag().setTtlDays(7);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service = new FraudDetectionServiceImpl(redisTemplate, properties, rabbitTemplate);
    }

    @Test
    void recordBookingAttempt_underThreshold_doesNotFlag() {
        String userId = "user1";
        when(valueOperations.increment(anyString())).thenReturn(3L);

        service.recordBookingAttempt(userId);

        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(FraudAlertEvent.class));
    }

    @Test
    void recordBookingAttempt_exactlyAtThreshold_doesNotFlag() {
        String userId = "user1";
        when(valueOperations.increment(anyString())).thenReturn(5L);

        service.recordBookingAttempt(userId);

        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(FraudAlertEvent.class));
    }

    @Test
    void recordBookingAttempt_exceedsThreshold_flagsAndPublishes() {
        String userId = "user1";
        when(valueOperations.increment(anyString())).thenReturn(6L);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);

        service.recordBookingAttempt(userId);

        // Verify flag was set
        verify(valueOperations).setIfAbsent(
                eq("fraud:flag:user:user1:HIGH_BOOKING_VELOCITY"),
                eq("6"),
                eq(7L),
                eq(TimeUnit.DAYS)
        );

        // Verify event published
        ArgumentCaptor<FraudAlertEvent> captor = ArgumentCaptor.forClass(FraudAlertEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.FRAUD_EXCHANGE),
                eq("fraud.alert"),
                captor.capture()
        );

        FraudAlertEvent event = captor.getValue();
        assertThat(event.getUserId()).isEqualTo("user1");
        assertThat(event.getAlertType()).isEqualTo(FraudAlertType.HIGH_BOOKING_VELOCITY);
        assertThat(event.getTriggeredThreshold()).isEqualTo(5);
        assertThat(event.getCurrentCounterValue()).isEqualTo(6);
    }

    @Test
    void recordBookingAttempt_alreadyFlagged_doesNotPublishDuplicate() {
        String userId = "user1";
        when(valueOperations.increment(anyString())).thenReturn(7L);
        // setIfAbsent returns false if key already exists (already flagged)
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);

        service.recordBookingAttempt(userId);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(FraudAlertEvent.class));
    }

    @Test
    void recordCancellation_exceedsThreshold_flagsAndPublishes() {
        String userId = "user2";
        when(valueOperations.increment(anyString())).thenReturn(4L); // Limit is 3
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);

        service.recordCancellation(userId);

        verify(valueOperations).setIfAbsent(
                eq("fraud:flag:user:user2:EXCESSIVE_CANCELLATIONS"),
                eq("4"),
                eq(7L),
                eq(TimeUnit.DAYS)
        );

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(FraudAlertEvent.class));
    }

    @Test
    void recordBookingAttempt_firstIncrement_setsExpiry() {
        String userId = "user3";
        when(valueOperations.increment(anyString())).thenReturn(1L);

        service.recordBookingAttempt(userId);

        verify(redisTemplate).expire(eq("fraud:booking:user:user3"), eq(1L), eq(TimeUnit.HOURS));
    }
}

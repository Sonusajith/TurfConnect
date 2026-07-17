package com.turfconnect.fraud.service;

import com.turfconnect.fraud.config.FraudProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceImplConcurrentTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void testConcurrentBookingAttempts() throws InterruptedException {
        FraudProperties properties = new FraudProperties();
        properties.getThresholds().getBooking().setLimit(5);
        properties.getThresholds().getBooking().setWindowHours(1);
        properties.getFlag().setTtlDays(7);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        FraudDetectionServiceImpl service = new FraudDetectionServiceImpl(redisTemplate, properties, rabbitTemplate);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // Simulate atomic increment in Redis
        AtomicLong counter = new AtomicLong(0);
        when(valueOperations.increment(anyString())).thenAnswer(invocation -> counter.incrementAndGet());
        
        // Simulate setIfAbsent for flag setting
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenAnswer(invocation -> {
            // In reality this only returns true the very first time
            String val = invocation.getArgument(1);
            return "6".equals(val); // only true on the exact trigger threshold
        });

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    service.recordBookingAttempt("concurrent-user");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Start all threads at once
        latch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        // Expect 10 increments
        verify(valueOperations, times(10)).increment(anyString());
        // Expire should only be called once when increment returns 1
        verify(redisTemplate, times(1)).expire(anyString(), anyLong(), any());
        
        // Only one alert should have been sent (when setIfAbsent was true)
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}

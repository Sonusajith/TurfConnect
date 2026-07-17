package com.turfconnect.fraud.service;

import com.turfconnect.fraud.dto.FraudStatusResponse;
import com.turfconnect.shared.dto.event.FraudAlertType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudQueryService {

    private final StringRedisTemplate redisTemplate;

    private static final String BOOKING_KEY_PREFIX = "fraud:booking:user:";
    private static final String CANCEL_KEY_PREFIX = "fraud:cancel:user:";
    private static final String FLAG_KEY_PREFIX = "fraud:flag:user:";

    public FraudStatusResponse getFraudStatus(String userId) {
        String bookingCountStr = redisTemplate.opsForValue().get(BOOKING_KEY_PREFIX + userId);
        String cancelCountStr = redisTemplate.opsForValue().get(CANCEL_KEY_PREFIX + userId);
        
        long recentBookings = bookingCountStr != null ? Long.parseLong(bookingCountStr) : 0;
        long recentCancellations = cancelCountStr != null ? Long.parseLong(cancelCountStr) : 0;
        
        List<String> activeFlags = new ArrayList<>();
        
        for (FraudAlertType type : FraudAlertType.values()) {
            String flagKey = FLAG_KEY_PREFIX + userId + ":" + type.name();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(flagKey))) {
                activeFlags.add(type.name());
            }
        }
        
        return FraudStatusResponse.builder()
                .userId(userId)
                .recentBookings(recentBookings)
                .recentCancellations(recentCancellations)
                .activeFlags(activeFlags)
                .build();
    }
}

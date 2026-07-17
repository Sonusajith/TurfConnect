package com.turfconnect.fraud.service;

import com.turfconnect.fraud.dto.FraudStatusResponse;
import com.turfconnect.shared.dto.event.FraudAlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudQueryServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private FraudQueryService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new FraudQueryService(redisTemplate);
    }

    @Test
    void getFraudStatus_userNotFoundOrClean_returnsZeroes() {
        String userId = "user-clean";
        when(valueOperations.get("fraud:booking:user:user-clean")).thenReturn(null);
        when(valueOperations.get("fraud:cancel:user:user-clean")).thenReturn(null);
        
        when(redisTemplate.hasKey("fraud:flag:user:user-clean:HIGH_BOOKING_VELOCITY")).thenReturn(false);
        when(redisTemplate.hasKey("fraud:flag:user:user-clean:EXCESSIVE_CANCELLATIONS")).thenReturn(false);

        FraudStatusResponse response = service.getFraudStatus(userId);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRecentBookings()).isZero();
        assertThat(response.getRecentCancellations()).isZero();
        assertThat(response.getActiveFlags()).isEmpty();
    }

    @Test
    void getFraudStatus_userHasActivityAndFlags_returnsData() {
        String userId = "user-flagged";
        when(valueOperations.get("fraud:booking:user:user-flagged")).thenReturn("6");
        when(valueOperations.get("fraud:cancel:user:user-flagged")).thenReturn("2");
        
        when(redisTemplate.hasKey("fraud:flag:user:user-flagged:HIGH_BOOKING_VELOCITY")).thenReturn(true);
        when(redisTemplate.hasKey("fraud:flag:user:user-flagged:EXCESSIVE_CANCELLATIONS")).thenReturn(false);

        FraudStatusResponse response = service.getFraudStatus(userId);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRecentBookings()).isEqualTo(6);
        assertThat(response.getRecentCancellations()).isEqualTo(2);
        assertThat(response.getActiveFlags()).containsExactly("HIGH_BOOKING_VELOCITY");
    }
}

package com.turfconnect.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudStatusResponse {
    private String userId;
    private long recentBookings;
    private long recentCancellations;
    private List<String> activeFlags;
}

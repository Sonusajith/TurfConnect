package com.turfconnect.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private double totalRevenue;
    private long fraudAlerts; // will be 0 for turf-specific

    private double confirmationRate;
    private double cancellationRate;
    private double averageRevenuePerBooking;
}

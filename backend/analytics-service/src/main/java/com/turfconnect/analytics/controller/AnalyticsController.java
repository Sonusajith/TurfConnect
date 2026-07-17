package com.turfconnect.analytics.controller;

import com.turfconnect.analytics.dto.AnalyticsSummaryResponse;
import com.turfconnect.analytics.exception.InvalidDateRangeException;
import com.turfconnect.analytics.service.AnalyticsQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/platform")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AnalyticsSummaryResponse> getPlatformSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        validateDateRange(startDate, endDate);
        return ResponseEntity.ok(analyticsQueryService.getPlatformSummary(startDate, endDate));
    }

    @GetMapping("/turfs/{turfId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('TURF_OWNER') and @securityService.ownsTurf(authentication, #turfId))")
    public ResponseEntity<AnalyticsSummaryResponse> getTurfSummary(
            @PathVariable String turfId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        validateDateRange(startDate, endDate);
        return ResponseEntity.ok(analyticsQueryService.getTurfSummary(turfId, startDate, endDate));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("startDate cannot be after endDate");
        }
    }
}

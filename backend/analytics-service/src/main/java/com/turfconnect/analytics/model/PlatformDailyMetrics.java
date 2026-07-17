package com.turfconnect.analytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "platform_daily_metrics")
public class PlatformDailyMetrics {

    @Id
    private String id;
    
    @org.springframework.data.mongodb.core.index.Indexed(unique = true)
    private LocalDate date;
    
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private double totalRevenue;
    
    private long fraudAlerts;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Set<String> processedEventIds = new HashSet<>();

    public PlatformDailyMetrics() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getFraudAlerts() {
        return fraudAlerts;
    }

    public void setFraudAlerts(long fraudAlerts) {
        this.fraudAlerts = fraudAlerts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<String> getProcessedEventIds() {
        return processedEventIds;
    }

    public void setProcessedEventIds(Set<String> processedEventIds) {
        this.processedEventIds = processedEventIds;
    }
}

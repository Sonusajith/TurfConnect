package com.turfconnect.fraud.service;

public interface FraudDetectionService {
    void recordBookingAttempt(String userId);
    void recordCancellation(String userId);
}

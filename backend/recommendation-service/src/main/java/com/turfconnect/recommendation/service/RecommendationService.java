package com.turfconnect.recommendation.service;

import com.turfconnect.recommendation.model.TurfProfile;

import java.util.List;

public interface RecommendationService {
    
    /**
     * Re-calculates and persists the heuristic score for a given turf.
     * Score = (Average Rating * Weight1) + (Normalized Bookings * Weight2)
     */
    void recalculateScore(String turfId);

    /**
     * Process a new booking event (ignoring cancelled bookings).
     */
    void processBookingEvent(String turfId, String bookingStatus);

    /**
     * Process a new review event.
     */
    void processReviewEvent(String turfId, double rating);

    /**
     * Get recommendations based on heuristic score.
     */
    List<TurfProfile> getRecommendations(String city, String sportType);
}

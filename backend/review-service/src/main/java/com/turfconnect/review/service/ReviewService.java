package com.turfconnect.review.service;

import com.turfconnect.shared.dto.review.ReviewCreateRequest;
import com.turfconnect.shared.dto.review.ReviewResponse;
import com.turfconnect.shared.dto.review.TurfRatingSummary;

import java.util.List;

public interface ReviewService {

    /**
     * Submit a new review and rating for a confirmed booking.
     */
    ReviewResponse submitReview(ReviewCreateRequest request, String userId);

    /**
     * Retrieve all active reviews for a specific turf.
     */
    List<ReviewResponse> getReviewsByTurf(String turfId);

    /**
     * Retrieve the average rating and review counts using database aggregation.
     */
    TurfRatingSummary getRatingSummary(String turfId);

    // ==========================================
    // Future-ready service layer placeholders
    // ==========================================

    /**
     * Update an existing review rating and comment (future-ready).
     */
    ReviewResponse updateReview(String reviewId, String userId, Integer rating, String comment);

    /**
     * Soft delete an existing review (future-ready).
     */
    void deleteReview(String reviewId, String userId);

    /**
     * Add owner response to a turf review (future-ready).
     */
    ReviewResponse addOwnerReply(String reviewId, String replyText);
}

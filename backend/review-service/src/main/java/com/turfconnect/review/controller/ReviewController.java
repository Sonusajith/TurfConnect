package com.turfconnect.review.controller;

import com.turfconnect.review.service.ReviewService;
import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.review.ReviewCreateRequest;
import com.turfconnect.shared.dto.review.ReviewResponse;
import com.turfconnect.shared.dto.review.TurfRatingSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Submit a new review for a verified slot booking.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReviewCreateRequest request) {

        ReviewResponse response = reviewService.submitReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Retrieve all active reviews for a specific turf.
     */
    @GetMapping("/turf/{turfId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByTurf(
            @PathVariable String turfId) {

        List<ReviewResponse> response = reviewService.getReviewsByTurf(turfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieve rating summary (average score and total reviews) for a turf.
     */
    @GetMapping("/turf/{turfId}/summary")
    public ResponseEntity<ApiResponse<TurfRatingSummary>> getRatingSummary(
            @PathVariable String turfId) {

        TurfRatingSummary response = reviewService.getRatingSummary(turfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.turfconnect.review.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turfconnect.review.model.Review;
import com.turfconnect.review.model.ReviewStatus;
import com.turfconnect.review.repository.ReviewRepository;
import com.turfconnect.review.service.ReviewService;
import com.turfconnect.shared.cache.CacheKeyUtil;
import com.turfconnect.shared.cache.CacheProperties;
import com.turfconnect.shared.dto.event.ReviewEvent;
import com.turfconnect.shared.dto.review.ReviewCreateRequest;
import com.turfconnect.shared.dto.review.ReviewResponse;
import com.turfconnect.shared.dto.review.TurfRatingSummary;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${services.booking-service.url:http://localhost:8083}")
    private String bookingServiceUrl;

    @Override
    public ReviewResponse submitReview(ReviewCreateRequest request, String userId) {
        log.info("Received review submission request for bookingId: {} by user: {}", request.getBookingId(), userId);

        // 1. Rating Range Check
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5.");
        }

        // 2. Duplicate Check: booking can only be reviewed once
        if (reviewRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new BadRequestException("This booking has already been reviewed.");
        }

        // 3. Fetch & Validate Booking details from booking-service via REST
        BookingData booking = fetchBookingDetails(request.getBookingId());

        // Verify booking owner matches
        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only review bookings that you own.");
        }

        // Verify booking status is CONFIRMED (meaning checkout/payment confirmed)
        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Cannot submit a review for a cancelled booking.");
        }
        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Cannot submit a review for an unconfirmed booking.");
        }

        // 4. Save Review
        Review review = Review.builder()
                .userId(userId)
                .userName(booking.getUserName())
                .userEmail(booking.getUserEmail())
                .bookingId(request.getBookingId())
                .turfId(booking.getTurfId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review successfully saved with ID: {}", saved.getId());

        // 5. Evict review cache for this turf (new review added)
        evictReviewCache(booking.getTurfId());

        // 6. Calculate Average Rating asynchronously using Aggregation
        TurfRatingSummary summary = getRatingSummary(booking.getTurfId());

        // 7. Publish ReviewEvent
        publishReviewEvent(booking.getTurfId(), summary);

        return toReviewResponse(saved);
    }

    /**
     * Cache-aside: check Redis first, fall back to MongoDB on miss.
     * If Redis is unavailable, transparently queries MongoDB (graceful degradation).
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ReviewResponse> getReviewsByTurf(String turfId) {
        String cacheKey = CacheKeyUtil.reviewsByTurf(turfId);

        // 1. Try cache
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("CACHE HIT  — key={}", cacheKey);
                return (List<ReviewResponse>) cached;
            }
            log.debug("CACHE MISS — key={}", cacheKey);
        } catch (Exception e) {
            log.warn("CACHE FAILURE (get) — key={}, error={}", cacheKey, e.getMessage());
        }

        // 2. Cache miss — hit MongoDB
        List<ReviewResponse> reviews = reviewRepository.findByTurfId(turfId).stream()
                .filter(r -> r.getStatus() == ReviewStatus.ACTIVE && !r.getIsDeleted())
                .map(this::toReviewResponse)
                .collect(Collectors.toList());

        // 3. Populate cache
        try {
            redisTemplate.opsForValue().set(cacheKey, reviews,
                    cacheProperties.getReviewsTurfTtlSeconds(), TimeUnit.SECONDS);
            log.debug("CACHE WRITE — key={}, ttl={}s", cacheKey, cacheProperties.getReviewsTurfTtlSeconds());
        } catch (Exception e) {
            log.warn("CACHE FAILURE (put) — key={}, error={}", cacheKey, e.getMessage());
        }

        return reviews;
    }

    @Override
    public TurfRatingSummary getRatingSummary(String turfId) {
        Criteria criteria = Criteria.where("turfId").is(turfId)
                .and("status").is(ReviewStatus.ACTIVE)
                .and("isDeleted").is(false);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("turfId")
                        .avg("rating").as("averageRating")
                        .count().as("totalReviews")
        );

        AggregationResults<RatingAggregateResult> results = mongoTemplate.aggregate(
                aggregation, Review.class, RatingAggregateResult.class
        );

        RatingAggregateResult result = results.getUniqueMappedResult();
        if (result != null) {
            // Round average rating to 2 decimal places
            double roundedAvg = Math.round(result.getAverageRating() * 100.0) / 100.0;
            return new TurfRatingSummary(roundedAvg, (int) result.getTotalReviews());
        }
        return new TurfRatingSummary(0.0, 0);
    }

    // ==========================================
    // Future-ready service layer placeholders
    // ==========================================

    @Override
    public ReviewResponse updateReview(String reviewId, String userId, Integer rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to edit this review.");
        }

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5.");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setIsEdited(true);
        review.setUpdatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);

        // Evict review cache (content changed)
        evictReviewCache(review.getTurfId());

        // Re-aggregate and publish event
        TurfRatingSummary summary = getRatingSummary(review.getTurfId());
        publishReviewEvent(review.getTurfId(), summary);

        return toReviewResponse(saved);
    }

    @Override
    public void deleteReview(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this review.");
        }

        review.setIsDeleted(true);
        review.setStatus(ReviewStatus.DELETED);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        // Evict review cache (review soft-deleted)
        evictReviewCache(review.getTurfId());

        // Re-aggregate and publish event
        TurfRatingSummary summary = getRatingSummary(review.getTurfId());
        publishReviewEvent(review.getTurfId(), summary);
    }

    @Override
    public ReviewResponse addOwnerReply(String reviewId, String replyText) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        review.setOwnerReply(replyText);
        review.setUpdatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);

        // Owner reply changes the cached review list
        evictReviewCache(review.getTurfId());

        return toReviewResponse(saved);
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    /**
     * Evict cached review list for a turf.
     * Graceful degradation: logs failure but never throws — the write operation
     * that triggered eviction must always succeed even if Redis is down.
     */
    private void evictReviewCache(String turfId) {
        String cacheKey = CacheKeyUtil.reviewsByTurf(turfId);
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.info("CACHE EVICT — key={}, deleted={}", cacheKey, deleted);
        } catch (Exception e) {
            log.warn("CACHE FAILURE (evict) — key={}, error={}", cacheKey, e.getMessage());
        }
    }

    private BookingData fetchBookingDetails(String bookingId) {
        try {
            String url = bookingServiceUrl + "/api/v1/bookings/" + bookingId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() == null) {
                throw new BadRequestException("Booking service returned an empty response.");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            boolean success = root.path("success").asBoolean();
            if (!success) {
                throw new BadRequestException("Booking service returned failure: " + root.path("message").asText());
            }

            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new ResourceNotFoundException("Booking details not found in response.");
            }

            return BookingData.builder()
                    .id(dataNode.path("id").asText())
                    .userId(dataNode.path("userId").asText())
                    .userName(dataNode.path("userName").asText(null))
                    .userEmail(dataNode.path("userEmail").asText(null))
                    .turfId(dataNode.path("turfId").asText())
                    .status(dataNode.path("status").asText())
                    .build();

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        } catch (Exception e) {
            log.error("Failed to fetch booking details for bookingId: " + bookingId, e);
            throw new BadRequestException("Unable to verify booking details at this time: " + e.getMessage());
        }
    }

    private void publishReviewEvent(String turfId, TurfRatingSummary summary) {
        try {
            ReviewEvent event = ReviewEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("REVIEW_UPDATED")
                    .version("1.0")
                    .timestamp(LocalDateTime.now())
                    .turfId(turfId)
                    .averageRating(summary.getAverageRating())
                    .totalReviews(summary.getTotalReviews())
                    .build();

            rabbitTemplate.convertAndSend(com.turfconnect.review.config.RabbitMQConfig.REVIEW_EXCHANGE, "review.updated", event);
            log.info("Successfully published ReviewEvent to RabbitMQ for turfId: {}", turfId);
        } catch (Exception e) {
            log.error("Failed to publish ReviewEvent to RabbitMQ", e);
        }
    }

    private ReviewResponse toReviewResponse(Review review) {
        BookingData booking = null;
        if ((review.getUserName() == null || review.getUserName().isBlank()) && review.getBookingId() != null) {
            booking = fetchBookingDetailsSafely(review.getBookingId());
        }

        String userName = review.getUserName();
        String userEmail = review.getUserEmail();
        if (booking != null) {
            if (userName == null || userName.isBlank()) {
                userName = booking.getUserName();
            }
            if (userEmail == null || userEmail.isBlank()) {
                userEmail = booking.getUserEmail();
            }
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .userName(userName)
                .userEmail(userEmail)
                .bookingId(review.getBookingId())
                .turfId(review.getTurfId())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus().name())
                .isEdited(review.getIsEdited())
                .isDeleted(review.getIsDeleted())
                .ownerReply(review.getOwnerReply())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private BookingData fetchBookingDetailsSafely(String bookingId) {
        try {
            return fetchBookingDetails(bookingId);
        } catch (Exception e) {
            log.warn("Unable to enrich review with booking customer details for bookingId={}: {}", bookingId, e.getMessage());
            return null;
        }
    }

    // Static helper models for deserialization & Mongo group aggregation mapping
    @Data
    @Builder
    public static class BookingData {
        private String id;
        private String userId;
        private String userName;
        private String userEmail;
        private String turfId;
        private String status;
    }

    @Data
    public static class RatingAggregateResult {
        private String id;
        private Double averageRating;
        private long totalReviews;
    }
}

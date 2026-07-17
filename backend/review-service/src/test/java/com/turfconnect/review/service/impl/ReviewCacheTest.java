package com.turfconnect.review.service.impl;

import com.turfconnect.review.model.Review;
import com.turfconnect.review.model.ReviewStatus;
import com.turfconnect.review.repository.ReviewRepository;
import com.turfconnect.shared.cache.CacheKeyUtil;
import com.turfconnect.shared.cache.CacheProperties;
import com.turfconnect.shared.dto.review.ReviewResponse;
import com.turfconnect.shared.dto.review.TurfRatingSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests focused on the Caching layer in ReviewServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ReviewCacheTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private CacheProperties cacheProperties;

    @BeforeEach
    void setUp() {
        cacheProperties = new CacheProperties();
        cacheProperties.setReviewsTurfTtlSeconds(300); // 5 minutes
        
        ReflectionTestUtils.setField(reviewService, "cacheProperties", cacheProperties);
        ReflectionTestUtils.setField(reviewService, "redisTemplate", redisTemplate);
    }

    @Test
    @DisplayName("getReviewsByTurf: Cache Hit returns list of ReviewResponses immediately")
    void getReviewsByTurf_cacheHit() {
        String turfId = "t-1";
        String key = CacheKeyUtil.reviewsByTurf(turfId);
        List<ReviewResponse> mockCachedResponse = List.of(
                ReviewResponse.builder().id("r-1").comment("Good").rating(4).build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(key)).thenReturn(mockCachedResponse);

        List<ReviewResponse> result = reviewService.getReviewsByTurf(turfId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("r-1");
        verify(reviewRepository, never()).findByTurfId(anyString());
    }

    @Test
    @DisplayName("getReviewsByTurf: Cache Miss reads from DB and populates cache")
    void getReviewsByTurf_cacheMiss() {
        String turfId = "t-1";
        String key = CacheKeyUtil.reviewsByTurf(turfId);
        Review dbReview = Review.builder()
                .id("r-1")
                .turfId(turfId)
                .rating(4)
                .comment("Good")
                .status(ReviewStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(key)).thenReturn(null);
        when(reviewRepository.findByTurfId(turfId)).thenReturn(List.of(dbReview));

        List<ReviewResponse> result = reviewService.getReviewsByTurf(turfId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("r-1");
        verify(valueOps).set(eq(key), any(List.class), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("getReviewsByTurf: Graceful degradation on Redis exception reads DB")
    void getReviewsByTurf_gracefulDegradation() {
        String turfId = "t-1";
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Redis down"));
        Review dbReview = Review.builder()
                .id("r-1")
                .turfId(turfId)
                .rating(4)
                .comment("Good")
                .status(ReviewStatus.ACTIVE)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        when(reviewRepository.findByTurfId(turfId)).thenReturn(List.of(dbReview));

        List<ReviewResponse> result = reviewService.getReviewsByTurf(turfId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("r-1");
        // No exception is thrown out of getReviewsByTurf
    }

    @Test
    @DisplayName("evictReviewCache is called during updateReview")
    void updateReview_evictsCache() {
        String reviewId = "r-1";
        String turfId = "t-1";
        String cacheKey = CacheKeyUtil.reviewsByTurf(turfId);
        Review review = Review.builder()
                .id(reviewId)
                .turfId(turfId)
                .userId("u-1")
                .rating(4)
                .comment("Good")
                .status(ReviewStatus.ACTIVE)
                .isDeleted(false)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        // Suppress aggregation framework calls in test if needed, or mock
        mongoTemplateAggregate();

        reviewService.updateReview(reviewId, "u-1", 5, "Better");

        verify(redisTemplate).delete(cacheKey);
    }

    @Test
    @DisplayName("evictReviewCache is called during deleteReview")
    void deleteReview_evictsCache() {
        String reviewId = "r-1";
        String turfId = "t-1";
        String cacheKey = CacheKeyUtil.reviewsByTurf(turfId);
        Review review = Review.builder()
                .id(reviewId)
                .turfId(turfId)
                .userId("u-1")
                .rating(4)
                .comment("Good")
                .status(ReviewStatus.ACTIVE)
                .isDeleted(false)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        mongoTemplateAggregate();

        reviewService.deleteReview(reviewId, "u-1");

        verify(redisTemplate).delete(cacheKey);
    }

    private TurfRatingSummary mongoTemplateAggregate() {
        // Helper to mimic getRatingSummary database returns
        org.springframework.data.mongodb.core.aggregation.AggregationResults<ReviewServiceImpl.RatingAggregateResult> mockResults = mock(org.springframework.data.mongodb.core.aggregation.AggregationResults.class);
        ReviewServiceImpl.RatingAggregateResult aggregateResult = new ReviewServiceImpl.RatingAggregateResult();
        aggregateResult.setId("t-1");
        aggregateResult.setAverageRating(4.0);
        aggregateResult.setTotalReviews(1);
        when(mockResults.getUniqueMappedResult()).thenReturn(aggregateResult);
        lenient().when(mongoTemplate.aggregate(any(org.springframework.data.mongodb.core.aggregation.Aggregation.class), eq(Review.class), eq(ReviewServiceImpl.RatingAggregateResult.class)))
                .thenReturn(mockResults);
        return new TurfRatingSummary(4.0, 1);
    }

    @Mock
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;
}

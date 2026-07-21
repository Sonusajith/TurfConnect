package com.turfconnect.review.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turfconnect.review.model.Review;
import com.turfconnect.review.model.ReviewStatus;
import com.turfconnect.review.repository.ReviewRepository;
import com.turfconnect.shared.dto.review.ReviewCreateRequest;
import com.turfconnect.shared.dto.review.ReviewResponse;
import com.turfconnect.shared.dto.review.TurfRatingSummary;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reviewService, "bookingServiceUrl", "http://localhost:8083");
    }

    @Test
    void submitReview_InvalidRating_ThrowsBadRequestException() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .bookingId("b-1")
                .rating(6) // Invalid rating
                .comment("Great!")
                .build();

        assertThrows(BadRequestException.class, () ->
                reviewService.submitReview(request, "u-1")
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void submitReview_DuplicateSubmission_ThrowsBadRequestException() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .bookingId("b-1")
                .rating(5)
                .comment("Excellent!")
                .build();

        Review existing = Review.builder().id("r-1").bookingId("b-1").build();
        when(reviewRepository.findByBookingId("b-1")).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () ->
                reviewService.submitReview(request, "u-1")
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void submitReview_OwnerMismatch_ThrowsForbiddenException() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .bookingId("b-1")
                .rating(5)
                .comment("Awesome!")
                .build();

        when(reviewRepository.findByBookingId("b-1")).thenReturn(Optional.empty());

        // Booking belongs to u-2, but u-1 is trying to submit review
        String bookingJson = "{\"success\":true,\"message\":\"Ok\",\"data\":{\"id\":\"b-1\",\"userId\":\"u-2\",\"turfId\":\"t-1\",\"status\":\"CONFIRMED\"}}";
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(bookingJson));

        assertThrows(ForbiddenException.class, () ->
                reviewService.submitReview(request, "u-1")
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void submitReview_UnconfirmedBooking_ThrowsBadRequestException() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .bookingId("b-1")
                .rating(5)
                .comment("Awesome!")
                .build();

        when(reviewRepository.findByBookingId("b-1")).thenReturn(Optional.empty());

        // Booking is still pending
        String bookingJson = "{\"success\":true,\"message\":\"Ok\",\"data\":{\"id\":\"b-1\",\"userId\":\"u-1\",\"turfId\":\"t-1\",\"status\":\"PENDING\"}}";
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(bookingJson));

        assertThrows(BadRequestException.class, () ->
                reviewService.submitReview(request, "u-1")
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void submitReview_SuccessfulSubmission_SavesAndPublishesEvent() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .bookingId("b-1")
                .rating(5)
                .comment("Awesome!")
                .build();

        when(reviewRepository.findByBookingId("b-1")).thenReturn(Optional.empty());

        String bookingJson = "{\"success\":true,\"message\":\"Ok\",\"data\":{\"id\":\"b-1\",\"userId\":\"u-1\",\"userName\":\"Saif Player\",\"userEmail\":\"saif.player@turfconnect.test\",\"turfId\":\"t-1\",\"status\":\"CONFIRMED\"}}";
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(bookingJson));

        Review saved = Review.builder()
                .id("r-10")
                .userId("u-1")
                .userName("Saif Player")
                .userEmail("saif.player@turfconnect.test")
                .bookingId("b-1")
                .turfId("t-1")
                .rating(5)
                .comment("Awesome!")
                .status(ReviewStatus.ACTIVE)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        // Mock aggregation template results for average calculation
        AggregationResults<ReviewServiceImpl.RatingAggregateResult> mockResults = mock(AggregationResults.class);
        ReviewServiceImpl.RatingAggregateResult aggregateResult = new ReviewServiceImpl.RatingAggregateResult();
        aggregateResult.setId("t-1");
        aggregateResult.setAverageRating(5.0);
        aggregateResult.setTotalReviews(1);
        when(mockResults.getUniqueMappedResult()).thenReturn(aggregateResult);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq(Review.class), eq(ReviewServiceImpl.RatingAggregateResult.class)))
                .thenReturn(mockResults);

        ReviewResponse response = reviewService.submitReview(request, "u-1");

        assertNotNull(response);
        assertEquals("r-10", response.getId());
        assertEquals("u-1", response.getUserId());
        assertEquals("Saif Player", response.getUserName());
        assertEquals("saif.player@turfconnect.test", response.getUserEmail());
        assertEquals(5, response.getRating());

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("review.exchange"), eq("review.updated"), any(com.turfconnect.shared.dto.event.ReviewEvent.class));
    }
}

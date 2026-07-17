package com.turfconnect.recommendation.service;

import com.turfconnect.recommendation.client.TurfServiceClient;
import com.turfconnect.recommendation.dto.TurfMetadataResponse;
import com.turfconnect.recommendation.model.TurfProfile;
import com.turfconnect.recommendation.repository.TurfProfileRepository;
import com.turfconnect.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationServiceImpl Tests")
class RecommendationServiceImplTest {

    @Mock private TurfProfileRepository turfProfileRepository;
    @Mock private TurfServiceClient turfServiceClient;
    
    @InjectMocks private RecommendationServiceImpl recommendationService;

    private final String TURF_ID = "turf-1";
    private TurfProfile existingProfile;

    @BeforeEach
    void setUp() {
        existingProfile = TurfProfile.builder()
                .turfId(TURF_ID)
                .totalBookings(5)
                .averageRating(4.0)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("processBookingEvent: ignores cancelled bookings")
    void processBookingEvent_ignoresCancelled() {
        recommendationService.processBookingEvent(TURF_ID, "CANCELLED");
        
        verify(turfProfileRepository, never()).findByTurfId(any());
        verify(turfProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("processBookingEvent: creates new profile if missing")
    void processBookingEvent_createNewProfile() {
        when(turfProfileRepository.findByTurfId(TURF_ID)).thenReturn(Optional.empty(), Optional.of(existingProfile));
        
        TurfMetadataResponse metadata = TurfMetadataResponse.builder()
                .name("New Turf")
                .city("CityA")
                .sportTypes(List.of("Football"))
                .active(true)
                .build();
        when(turfServiceClient.getTurfDetails(TURF_ID)).thenReturn(ApiResponse.success(metadata));
        
        // This save happens in getOrCreateProfile
        when(turfProfileRepository.save(any(TurfProfile.class))).thenAnswer(i -> {
            TurfProfile p = i.getArgument(0);
            if (p.getTotalBookings() == 0) { // the initial save
                return p;
            }
            return p; // the increment save
        });

        recommendationService.processBookingEvent(TURF_ID, "CONFIRMED");

        verify(turfServiceClient).getTurfDetails(TURF_ID);
        verify(turfProfileRepository, atLeast(2)).save(any(TurfProfile.class));
    }

    @Test
    @DisplayName("processReviewEvent: recalculates score correctly")
    void processReviewEvent_recalculatesScore() {
        when(turfProfileRepository.findByTurfId(TURF_ID)).thenReturn(Optional.of(existingProfile));
        when(turfProfileRepository.save(any(TurfProfile.class))).thenAnswer(i -> i.getArgument(0));

        recommendationService.processReviewEvent(TURF_ID, 5.0);

        assertThat(existingProfile.getAverageRating()).isEqualTo(5.0);
        // Normalized rating = 5.0/5.0 = 1.0. Weight = 0.7.
        // Normalized bookings = 5/1000 = 0.005. Weight = 0.3.
        // Score = 0.7 + (0.005 * 0.3) = 0.7015
        assertThat(existingProfile.getHeuristicScore()).isEqualTo(0.7015);
    }
}

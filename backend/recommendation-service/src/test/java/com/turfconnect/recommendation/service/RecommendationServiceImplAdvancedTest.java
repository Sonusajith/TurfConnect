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
@DisplayName("RecommendationServiceImpl Advanced Tests")
class RecommendationServiceImplAdvancedTest {

    @Mock private TurfProfileRepository turfProfileRepository;
    @Mock private TurfServiceClient turfServiceClient;

    @InjectMocks private RecommendationServiceImpl recommendationService;

    private final String TURF_ID = "turf-1";
    
    @Test
    @DisplayName("Fallback metadata gracefully handles client failure")
    void getOrCreateProfile_handlesClientFailure() {
        when(turfProfileRepository.findByTurfId(TURF_ID)).thenReturn(Optional.empty());
        when(turfServiceClient.getTurfDetails(TURF_ID)).thenThrow(new RuntimeException("Service down"));
        
        when(turfProfileRepository.save(any(TurfProfile.class))).thenAnswer(i -> i.getArgument(0));

        recommendationService.processBookingEvent(TURF_ID, "CONFIRMED");

        verify(turfServiceClient).getTurfDetails(TURF_ID);
        // It should still save a profile even without metadata
        verify(turfProfileRepository, atLeastOnce()).save(argThat(profile -> 
            profile.getTurfId().equals(TURF_ID) && profile.getTurfName() == null
        ));
    }

    @Test
    @DisplayName("Recommendation ordering only includes active turfs")
    void getRecommendations_filtersInactive() {
        TurfProfile active1 = TurfProfile.builder().turfId("1").heuristicScore(0.8).isActive(true).build();
        TurfProfile active2 = TurfProfile.builder().turfId("2").heuristicScore(0.6).isActive(true).build();
        
        when(turfProfileRepository.findByCityAndIsActiveTrueOrderByHeuristicScoreDesc("CityA"))
                .thenReturn(List.of(active1, active2));

        List<TurfProfile> results = recommendationService.getRecommendations("CityA", null);
        
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getHeuristicScore()).isGreaterThan(results.get(1).getHeuristicScore());
    }
}

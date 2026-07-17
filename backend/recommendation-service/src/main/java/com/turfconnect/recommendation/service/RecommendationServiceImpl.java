package com.turfconnect.recommendation.service;

import com.turfconnect.recommendation.client.TurfServiceClient;
import com.turfconnect.recommendation.dto.TurfMetadataResponse;
import com.turfconnect.recommendation.model.TurfProfile;
import com.turfconnect.recommendation.repository.TurfProfileRepository;
import com.turfconnect.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final TurfProfileRepository turfProfileRepository;
    private final TurfServiceClient turfServiceClient;

    // Modular weights for the heuristic (can be extracted to config/properties)
    private static final double WEIGHT_RATING = 0.7;
    private static final double WEIGHT_POPULARITY = 0.3;
    private static final int NORMALIZATION_MAX_BOOKINGS = 1000; // max threshold for popularity normalization

    @Override
    public void processBookingEvent(String turfId, String bookingStatus) {
        if ("CANCELLED".equalsIgnoreCase(bookingStatus)) {
            log.info("Ignoring cancelled booking for turf {}", turfId);
            return;
        }

        TurfProfile profile = getOrCreateProfile(turfId);
        profile.setTotalBookings(profile.getTotalBookings() + 1);
        turfProfileRepository.save(profile);
        
        recalculateScore(turfId);
    }

    @Override
    public void processReviewEvent(String turfId, double rating) {
        if (rating < 0 || rating > 5) {
            log.warn("Ignoring invalid rating {} for turf {}", rating, turfId);
            return;
        }

        TurfProfile profile = getOrCreateProfile(turfId);
        
        // Simple moving average heuristic for now (assumes equal weight or delegates to review-service for exact avg)
        // In reality, we might fetch the exact new average from review-service, but here we approximate or update.
        // For accurate tracking, let's assume the event passes the *new average* or we just use a simple dampening formula.
        // Actually, let's just assume the event 'rating' is the new average rating for the turf, OR we keep a count.
        // Since we didn't add reviewCount to TurfProfile, let's assume 'rating' is the new overall average calculated by review-service.
        profile.setAverageRating(rating);
        turfProfileRepository.save(profile);

        recalculateScore(turfId);
    }

    @Override
    public void recalculateScore(String turfId) {
        Optional<TurfProfile> optionalProfile = turfProfileRepository.findByTurfId(turfId);
        if (optionalProfile.isEmpty()) {
            return;
        }

        TurfProfile profile = optionalProfile.get();
        
        // Normalize bookings (0 to 1)
        double normalizedBookings = Math.min(1.0, (double) profile.getTotalBookings() / NORMALIZATION_MAX_BOOKINGS);
        
        // Normalize rating (0 to 1)
        double normalizedRating = profile.getAverageRating() / 5.0;

        // Heuristic Algorithm
        double score = (normalizedRating * WEIGHT_RATING) + (normalizedBookings * WEIGHT_POPULARITY);
        
        profile.setHeuristicScore(score);
        profile.setUpdatedAt(Instant.now());
        turfProfileRepository.save(profile);
        
        log.debug("Recalculated score for turf {}: {}", turfId, score);
    }

    @Override
    public List<TurfProfile> getRecommendations(String city, String sportType) {
        if (city != null && sportType != null) {
            return turfProfileRepository.findByCityAndSportTypesContainingAndIsActiveTrueOrderByHeuristicScoreDesc(city, sportType);
        } else if (city != null) {
            return turfProfileRepository.findByCityAndIsActiveTrueOrderByHeuristicScoreDesc(city);
        } else if (sportType != null) {
            return turfProfileRepository.findBySportTypesContainingAndIsActiveTrueOrderByHeuristicScoreDesc(sportType);
        } else {
            return turfProfileRepository.findByIsActiveTrueOrderByHeuristicScoreDesc();
        }
    }

    private TurfProfile getOrCreateProfile(String turfId) {
        return turfProfileRepository.findByTurfId(turfId).orElseGet(() -> {
            log.info("Fetching metadata for new turf {}", turfId);
            TurfProfile newProfile = TurfProfile.builder()
                    .turfId(turfId)
                    .averageRating(0.0)
                    .totalBookings(0)
                    .heuristicScore(0.0)
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            try {
                ApiResponse<TurfMetadataResponse> response = turfServiceClient.getTurfDetails(turfId);
                if (response != null && response.getData() != null) {
                    TurfMetadataResponse metadata = response.getData();
                    newProfile.setTurfName(metadata.getName());
                    newProfile.setCity(metadata.getCity());
                    newProfile.setLocation(metadata.getLocation());
                    newProfile.setSportTypes(metadata.getSportTypes());
                    newProfile.setActive(metadata.isActive());
                } else {
                    log.warn("No metadata returned for turf {}", turfId);
                }
            } catch (Exception e) {
                log.error("Failed to fetch metadata for turf {}: {}", turfId, e.getMessage());
                // Leave fields null, can be reconciled later
            }

            return turfProfileRepository.save(newProfile);
        });
    }
}

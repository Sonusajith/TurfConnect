package com.turfconnect.recommendation.controller;

import com.turfconnect.recommendation.dto.RecommendationResponse;
import com.turfconnect.recommendation.service.RecommendationService;
import com.turfconnect.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/turfs")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sportType) {
        
        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(city, sportType)
                .stream()
                .map(profile -> RecommendationResponse.builder()
                        .turfId(profile.getTurfId())
                        .turfName(profile.getTurfName())
                        .city(profile.getCity())
                        .location(profile.getLocation())
                        .sportTypes(profile.getSportTypes())
                        .averageRating(profile.getAverageRating())
                        .totalBookings(profile.getTotalBookings())
                        .heuristicScore(profile.getHeuristicScore())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}

package com.turfconnect.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private String turfId;
    private String turfName;
    private String city;
    private String location;
    private List<String> sportTypes;
    private double averageRating;
    private int totalBookings;
    private double heuristicScore;
}

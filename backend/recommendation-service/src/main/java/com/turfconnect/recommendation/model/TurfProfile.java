package com.turfconnect.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "turf_profiles")
public class TurfProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String turfId;

    private String turfName;

    @Indexed
    private String city;

    private String location;

    @Indexed
    private List<String> sportTypes;

    private double averageRating;
    private int totalBookings;
    
    @Indexed
    private double heuristicScore;

    private boolean isActive;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

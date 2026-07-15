package com.turfconnect.turf.dto;

import com.turfconnect.turf.model.TurfImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfResponse {
    private String id;
    private String ownerId;
    private String name;
    private String description;
    private List<String> sportTypes;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    
    private Double longitude;
    private Double latitude;
    
    private String timezone;
    private BigDecimal hourlyRate;
    private String currency;
    private List<String> amenities;
    
    private List<TurfImage> images;
    private String coverImage;
    
    private String operatingHours;
    private List<String> availableDays;
    private String contactNumber;
    private String email;
    
    private Integer capacity;
    private String surfaceType;
    private String indoorOrOutdoor;
    
    private boolean floodlightsAvailable;
    private boolean parkingAvailable;
    private boolean changingRoomsAvailable;
    private boolean washroomsAvailable;
    private boolean drinkingWaterAvailable;
    private boolean equipmentRentalAvailable;
    private boolean foodAvailable;
    private boolean coachingAvailable;
    
    private Double averageRating;
    private Integer totalReviews;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

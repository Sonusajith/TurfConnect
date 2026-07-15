package com.turfconnect.turf.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfUpdateRequest {

    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private List<String> sportTypes;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    private Double longitude;

    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    private Double latitude;

    private String timezone;

    @Positive(message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;

    private String currency;
    private List<String> amenities;
    private String operatingHours;
    private List<String> availableDays;
    private String contactNumber;
    
    @Email(message = "Invalid email format")
    private String email;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private String surfaceType;
    private String indoorOrOutdoor;
    
    private Boolean floodlightsAvailable;
    private Boolean parkingAvailable;
    private Boolean changingRoomsAvailable;
    private Boolean washroomsAvailable;
    private Boolean drinkingWaterAvailable;
    private Boolean equipmentRentalAvailable;
    private Boolean foodAvailable;
    private Boolean coachingAvailable;
    
    private String status; // Allow owner to mark as INACTIVE or MAINTENANCE
}

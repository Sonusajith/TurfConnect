package com.turfconnect.turf.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfCreateRequest {

    @NotBlank(message = "Turf name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotEmpty(message = "At least one sport type is required")
    private List<String> sportTypes;

    private String organizationId;
    private String franchiseId;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String country;
    private String postalCode;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    private Double longitude;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    private Double latitude;

    private String timezone;

    @NotNull(message = "Hourly rate is required")
    @Positive(message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;

    private String currency;
    
    private List<String> amenities;
    private String coverImage;
    
    @NotNull(message = "Open time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    @NotNull(message = "Slot duration is required")
    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    @Max(value = 1440, message = "Slot duration cannot exceed 24 hours")
    private Integer slotDurationMinutes;

    private List<String> availableDays;
    
    @NotBlank(message = "Contact number is required")
    private String contactNumber;
    
    @Email(message = "Invalid email format")
    private String email;

    @Min(value = 1, message = "Capacity must be at least 1")
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
}

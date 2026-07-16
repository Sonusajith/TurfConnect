package com.turfconnect.turf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "turfs")
public class Turf {

    @Id
    private String id;
    
    @Indexed
    private String ownerId;
    
    private String venueId; // Future support for multi-ground venues
    
    private String name;
    private String description;
    
    @Indexed
    private List<String> sportTypes;
    
    private String address;
    
    @Indexed
    private String city;
    
    private String state;
    private String country;
    private String postalCode;
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;
    
    private String timezone;
    
    @Indexed
    private BigDecimal hourlyRate;
    private String currency;
    
    private List<String> amenities;
    
    private List<TurfImage> images;
    private String coverImage;
    
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotDurationMinutes; // defaults to 60
    
    private List<String> availableDays; // e.g. ["MON", "TUE", ...]
    
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
    
    @Indexed
    private Double averageRating;
    private Integer totalReviews;
    private Integer bookingCount;
    
    @Indexed
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, PENDING_APPROVAL, REJECTED
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private boolean deleted; // Soft delete flag
}

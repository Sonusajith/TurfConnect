package com.turfconnect.turf.dto;

import lombok.Data;

@Data
public class TurfSearchCriteria {
    private String city;
    private String sport;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private String surfaceType;
    private String indoorOrOutdoor;
    private Boolean floodlights;
    
    private Double longitude;
    private Double latitude;
    private Double radiusInMeters; // default e.g. 5000 (5km)
    
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}

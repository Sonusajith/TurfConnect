package com.turfconnect.turf.repository;

import com.turfconnect.turf.model.Turf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TurfRepositoryCustom {
    Page<Turf> searchTurfs(
            String city,
            String sport,
            Double minPrice,
            Double maxPrice,
            Double minRating,
            String surfaceType,
            String indoorOrOutdoor,
            Boolean floodlights,
            Double longitude,
            Double latitude,
            Double maxDistanceInMeters,
            Pageable pageable
    );
}

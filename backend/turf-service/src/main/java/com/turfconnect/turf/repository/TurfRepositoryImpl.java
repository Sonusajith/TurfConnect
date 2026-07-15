package com.turfconnect.turf.repository;

import com.turfconnect.turf.model.Turf;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TurfRepositoryImpl implements TurfRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Turf> searchTurfs(
            String city, String sport, Double minPrice, Double maxPrice, Double minRating,
            String surfaceType, String indoorOrOutdoor, Boolean floodlights,
            Double longitude, Double latitude, Double maxDistanceInMeters, Pageable pageable) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Exclude deleted turfs
        criteriaList.add(Criteria.where("deleted").is(false));
        criteriaList.add(Criteria.where("status").is("ACTIVE"));

        if (city != null && !city.isBlank()) {
            criteriaList.add(Criteria.where("city").regex("^" + city + "$", "i"));
        }
        if (sport != null && !sport.isBlank()) {
            criteriaList.add(Criteria.where("sportTypes").is(sport));
        }
        if (minPrice != null) {
            criteriaList.add(Criteria.where("hourlyRate").gte(minPrice));
        }
        if (maxPrice != null) {
            criteriaList.add(Criteria.where("hourlyRate").lte(maxPrice));
        }
        if (minRating != null) {
            criteriaList.add(Criteria.where("averageRating").gte(minRating));
        }
        if (surfaceType != null && !surfaceType.isBlank()) {
            criteriaList.add(Criteria.where("surfaceType").is(surfaceType));
        }
        if (indoorOrOutdoor != null && !indoorOrOutdoor.isBlank()) {
            criteriaList.add(Criteria.where("indoorOrOutdoor").is(indoorOrOutdoor));
        }
        if (floodlights != null && floodlights) {
            criteriaList.add(Criteria.where("floodlightsAvailable").is(true));
        }
        if (longitude != null && latitude != null && maxDistanceInMeters != null) {
            Point point = new Point(longitude, latitude);
            criteriaList.add(Criteria.where("location").nearSphere(point).maxDistance(maxDistanceInMeters));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long count = mongoTemplate.count(query, Turf.class);

        query.with(pageable);
        List<Turf> turfs = mongoTemplate.find(query, Turf.class);

        return new PageImpl<>(turfs, pageable, count);
    }
}

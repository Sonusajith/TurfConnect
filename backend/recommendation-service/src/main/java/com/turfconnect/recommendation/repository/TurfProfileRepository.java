package com.turfconnect.recommendation.repository;

import com.turfconnect.recommendation.model.TurfProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurfProfileRepository extends MongoRepository<TurfProfile, String> {
    Optional<TurfProfile> findByTurfId(String turfId);
    
    // Spring Data MongoDB handles query derivation
    List<TurfProfile> findByCityAndIsActiveTrueOrderByHeuristicScoreDesc(String city);
    List<TurfProfile> findBySportTypesContainingAndIsActiveTrueOrderByHeuristicScoreDesc(String sportType);
    List<TurfProfile> findByCityAndSportTypesContainingAndIsActiveTrueOrderByHeuristicScoreDesc(String city, String sportType);
    List<TurfProfile> findByIsActiveTrueOrderByHeuristicScoreDesc();
}

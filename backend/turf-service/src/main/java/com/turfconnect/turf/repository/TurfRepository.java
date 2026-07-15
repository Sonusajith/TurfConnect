package com.turfconnect.turf.repository;

import com.turfconnect.turf.model.Turf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TurfRepository extends MongoRepository<Turf, String>, TurfRepositoryCustom {
    Page<Turf> findByOwnerIdAndDeletedFalse(String ownerId, Pageable pageable);
    Optional<Turf> findByIdAndDeletedFalse(String id);
}

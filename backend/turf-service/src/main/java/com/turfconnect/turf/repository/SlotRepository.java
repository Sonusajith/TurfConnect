package com.turfconnect.turf.repository;

import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.turf.model.Slot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends MongoRepository<Slot, String> {
    List<Slot> findByTurfIdAndDate(String turfId, LocalDate date);
    List<Slot> findByTurfIdAndDateAndStatus(String turfId, LocalDate date, SlotStatus status);
    boolean existsByTurfIdAndDate(String turfId, LocalDate date);
}

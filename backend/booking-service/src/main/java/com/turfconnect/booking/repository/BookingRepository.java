package com.turfconnect.booking.repository;

import com.turfconnect.booking.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    Optional<Booking> findBySlotId(String slotId);
}

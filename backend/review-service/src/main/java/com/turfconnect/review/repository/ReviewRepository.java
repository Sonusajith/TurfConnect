package com.turfconnect.review.repository;

import com.turfconnect.review.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTurfId(String turfId);
    Optional<Review> findByBookingId(String bookingId);
}

package com.turfconnect.auth.repository;

import com.turfconnect.auth.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    void deleteByUserId(String userId);
    void deleteByTokenId(String tokenId);
}

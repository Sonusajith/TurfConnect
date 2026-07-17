package com.turfconnect.fraud.controller;

import com.turfconnect.fraud.dto.FraudStatusResponse;
import com.turfconnect.fraud.service.FraudQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudQueryService fraudQueryService;

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FraudStatusResponse> getUserFraudStatus(@PathVariable String userId) {
        try {
            FraudStatusResponse status = fraudQueryService.getFraudStatus(userId);
            
            // "No active fraud signals" -> returning 200 with 0 values
            // A dedicated field or just empty activeFlags list indicates this.
            
            return ResponseEntity.ok(status);
        } catch (RedisConnectionFailureException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}

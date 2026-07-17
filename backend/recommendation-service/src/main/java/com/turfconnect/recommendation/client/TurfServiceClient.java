package com.turfconnect.recommendation.client;

import com.turfconnect.recommendation.dto.TurfMetadataResponse;
import com.turfconnect.shared.dto.ApiResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "turf-service")
public interface TurfServiceClient {

    @GetMapping("/api/v1/turfs/{turfId}")
    @Cacheable(value = "turf-metadata", key = "#turfId", unless = "#result == null")
    ApiResponse<TurfMetadataResponse> getTurfDetails(@PathVariable("turfId") String turfId);
}

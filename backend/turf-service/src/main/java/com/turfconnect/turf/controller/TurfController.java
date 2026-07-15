package com.turfconnect.turf.controller;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.PageResponse;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.turf.dto.TurfCreateRequest;
import com.turfconnect.turf.dto.TurfResponse;
import com.turfconnect.turf.dto.TurfSearchCriteria;
import com.turfconnect.turf.dto.TurfUpdateRequest;
import com.turfconnect.turf.service.TurfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/turfs")
@RequiredArgsConstructor
public class TurfController {

    private final TurfService turfService;

    private void requireTurfOwner(String role) {
        if (!"TURF_OWNER".equals(role)) {
            throw new ForbiddenException("Only turf owners can perform this action");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TurfResponse>> createTurf(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody TurfCreateRequest request) {
        
        requireTurfOwner(role);
        TurfResponse response = turfService.createTurf(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TurfResponse>> updateTurf(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody TurfUpdateRequest request) {
        
        requireTurfOwner(role);
        TurfResponse response = turfService.updateTurf(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTurf(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        
        requireTurfOwner(role);
        turfService.deleteTurf(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TurfResponse>> getTurfById(@PathVariable String id) {
        TurfResponse response = turfService.getTurfById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TurfResponse>>> searchTurfs(
            @ModelAttribute TurfSearchCriteria criteria) {
        
        PageResponse<TurfResponse> response = turfService.searchTurfs(criteria);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-turfs")
    public ResponseEntity<ApiResponse<PageResponse<TurfResponse>>> getMyTurfs(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        requireTurfOwner(role);
        PageResponse<TurfResponse> response = turfService.getMyTurfs(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

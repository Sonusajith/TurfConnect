package com.turfconnect.turf.controller;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.turf.SlotDTO;
import com.turfconnect.shared.dto.turf.SlotGenerationRequest;
import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.turf.service.TurfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SlotController {

    private final TurfService turfService;

    private void requireTurfOwner(String role) {
        if (!"TURF_OWNER".equals(role)) {
            throw new ForbiddenException("Only turf owners can perform this action");
        }
    }

    @GetMapping("/api/v1/turfs/{turfId}/slots")
    public ResponseEntity<ApiResponse<List<SlotDTO>>> getSlots(
            @PathVariable String turfId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<SlotDTO> slots = turfService.getSlots(turfId, date);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @PostMapping("/api/v1/turfs/{turfId}/slots/generate")
    public ResponseEntity<ApiResponse<Void>> generateSlots(
            @PathVariable String turfId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody SlotGenerationRequest request) {
        
        requireTurfOwner(role);
        turfService.generateSlotsForDateRange(turfId, request.getStartDate(), request.getEndDate(), userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Internal endpoint only — not routed in API Gateway
    @PutMapping("/api/v1/internal/slots/{slotId}/status")
    public ResponseEntity<ApiResponse<SlotDTO>> updateSlotStatus(
            @PathVariable String slotId,
            @RequestParam SlotStatus status,
            @RequestParam(required = false) String bookingId) {
        
        SlotDTO updated = turfService.updateSlotStatus(slotId, status, bookingId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}

package com.turfconnect.booking.controller;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.booking.BookingCreateRequest;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.shared.dto.booking.SplitContributionRequest;
import com.turfconnect.shared.dto.booking.SplitContributionResponse;
import com.turfconnect.booking.service.BookingService;
import com.turfconnect.shared.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.turfconnect.shared.audit.AuditLog;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Value("${spring.security.internal-token:internal-secret-token}")
    private String internalTokenSecret;

    private void verifyInternalToken(String headerToken) {
        if (headerToken == null || !headerToken.equals(internalTokenSecret)) {
            throw new ForbiddenException("Invalid or missing internal service token.");
        }
    }

    @PostMapping
    @AuditLog(action = "CREATE_BOOKING", resource = "BOOKING")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BookingCreateRequest request) {
        
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable String id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @RequestHeader("X-User-Id") String userId) {
        
        List<BookingResponse> response = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/turf/{turfId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsForTurf(
            @PathVariable String turfId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        List<BookingResponse> response = bookingService.getBookingsForOwnedTurf(turfId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Internal-Token", required = false) String internalToken) {
        
        verifyInternalToken(internalToken);
        BookingResponse response = bookingService.confirmBooking(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/cancel")
    @AuditLog(action = "CANCEL_BOOKING", resource = "BOOKING")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        BookingResponse response = bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/split")
    @AuditLog(action = "UPDATE_BOOKING_SPLIT", resource = "BOOKING")
    public ResponseEntity<ApiResponse<SplitContributionResponse>> updateSplitContribution(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SplitContributionRequest request) {

        SplitContributionResponse response = bookingService.updateSplitContribution(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

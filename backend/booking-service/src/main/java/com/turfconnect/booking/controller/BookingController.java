package com.turfconnect.booking.controller;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.booking.BookingCreateRequest;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
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

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        
        BookingResponse response = bookingService.confirmBooking(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        
        BookingResponse response = bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

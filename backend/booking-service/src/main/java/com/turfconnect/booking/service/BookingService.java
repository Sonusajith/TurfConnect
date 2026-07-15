package com.turfconnect.booking.service;

import com.turfconnect.booking.model.Booking;
import com.turfconnect.booking.repository.BookingRepository;
import com.turfconnect.shared.dto.booking.BookingCreateRequest;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.shared.dto.booking.BookingStatus;
import com.turfconnect.shared.dto.turf.SlotDTO;
import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisLockService redisLockService;
    private final RestTemplate restTemplate;

    @Value("${services.turf-service.url:http://localhost:8082}")
    private String turfServiceUrl;

    public BookingResponse createBooking(BookingCreateRequest request, String userId) {
        String slotId = request.getSlotId();

        // 1. Fetch slot details from turf-service
        String slotUrl = turfServiceUrl + "/api/v1/internal/slots/" + slotId;
        SlotDTO slot;
        try {
            ResponseEntity<SlotResponseWrapper> response = restTemplate.getForEntity(slotUrl, SlotResponseWrapper.class);
            if (response.getBody() == null || !response.getBody().isSuccess() || response.getBody().getData() == null) {
                throw new ResourceNotFoundException("Slot not found with id: " + slotId);
            }
            slot = response.getBody().getData();
        } catch (Exception e) {
            log.error("Error calling turf-service for slot details", e);
            throw new ResourceNotFoundException("Slot not found with id: " + slotId);
        }

        // 2. Validate availability
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new BadRequestException("Slot is not available for booking. Status: " + slot.getStatus());
        }

        // 3. Validate price integrity
        if (request.getTotalPrice().compareTo(slot.getPrice()) != 0) {
            throw new BadRequestException("Price mismatch: expected " + slot.getPrice() + ", got " + request.getTotalPrice());
        }

        // 4. Acquire Redis distributed lock (TTL: 5 minutes = 300,000 ms)
        String lockKey = "lock:slot:" + slotId;
        String lockToken = UUID.randomUUID().toString();
        boolean lockAcquired = redisLockService.acquireLock(lockKey, lockToken, 300000);

        if (!lockAcquired) {
            throw new BadRequestException("Slot is temporarily locked by another user. Please try again in a few minutes.");
        }

        // 5. Save PENDING booking in MongoDB
        Booking booking = Booking.builder()
                .userId(userId)
                .slotId(slotId)
                .turfId(slot.getTurfId())
                .date(slot.getDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .totalPrice(slot.getPrice())
                .status(BookingStatus.PENDING)
                .lockToken(lockToken)
                .build();

        Booking savedBooking;
        try {
            savedBooking = bookingRepository.save(booking);
        } catch (DuplicateKeyException e) {
            // Uniqueness check for slotId violated at DB level
            redisLockService.releaseLock(lockKey, lockToken);
            throw new BadRequestException("Slot is already booked.");
        }

        // 6. Transition slot status to LOCKED in turf-service via REST
        try {
            String statusUrl = turfServiceUrl + "/api/v1/internal/slots/" + slotId + "/status?status=LOCKED&bookingId=" + savedBooking.getId();
            restTemplate.put(statusUrl, null);
        } catch (Exception e) {
            log.error("Failed to update slot status to LOCKED in turf-service. Rolling back...", e);
            // Transactional rollback
            bookingRepository.delete(savedBooking);
            redisLockService.releaseLock(lockKey, lockToken);
            throw new BadRequestException("Failed to initiate booking due to internal communication error.");
        }

        return toBookingResponse(savedBooking);
    }

    public BookingResponse confirmBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking cannot be confirmed. Status: " + booking.getStatus());
        }

        // Update database status
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        // Update slot status to BOOKED
        try {
            String statusUrl = turfServiceUrl + "/api/v1/internal/slots/" + booking.getSlotId() + "/status?status=BOOKED&bookingId=" + bookingId;
            restTemplate.put(statusUrl, null);
        } catch (Exception e) {
            log.error("Failed to mark slot as BOOKED in turf-service", e);
            // Critical warning: keep retrying or log for reconciler
        }

        // Release the Redis distributed lock
        String lockKey = "lock:slot:" + booking.getSlotId();
        redisLockService.releaseLock(lockKey, booking.getLockToken());

        return toBookingResponse(saved);
    }

    public BookingResponse cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled.");
        }

        // Update database status
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Update slot status back to AVAILABLE
        try {
            String statusUrl = turfServiceUrl + "/api/v1/internal/slots/" + booking.getSlotId() + "/status?status=AVAILABLE";
            restTemplate.put(statusUrl, null);
        } catch (Exception e) {
            log.error("Failed to reset slot status to AVAILABLE in turf-service", e);
        }

        // Release the Redis distributed lock
        String lockKey = "lock:slot:" + booking.getSlotId();
        redisLockService.releaseLock(lockKey, booking.getLockToken());

        return toBookingResponse(saved);
    }

    public BookingResponse getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return toBookingResponse(booking);
    }

    public List<BookingResponse> getMyBookings(String userId) {
        List<Booking> list = bookingRepository.findByUserId(userId);
        List<BookingResponse> dtos = new ArrayList<>();
        for (Booking b : list) {
            dtos.add(toBookingResponse(b));
        }
        return dtos;
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .slotId(booking.getSlotId())
                .turfId(booking.getTurfId())
                .date(booking.getDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    // Static helper inner class to handle deserialization of ApiResponse wrapped SlotDTO
    @Data
    public static class SlotResponseWrapper {
        private boolean success;
        private SlotDTO data;
        private String message;
    }
}

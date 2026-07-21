package com.turfconnect.booking.service;

import com.turfconnect.booking.model.Booking;
import com.turfconnect.booking.repository.BookingRepository;
import com.turfconnect.shared.dto.booking.BookingCreateRequest;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.shared.dto.booking.BookingStatus;
import com.turfconnect.shared.dto.booking.SplitContributionMember;
import com.turfconnect.shared.dto.booking.SplitContributionRequest;
import com.turfconnect.shared.dto.booking.SplitContributionResponse;
import com.turfconnect.shared.dto.booking.SplitContributionStatus;
import com.turfconnect.shared.dto.payment.PaymentStatus;
import com.turfconnect.shared.dto.payment.RefundRequest;
import com.turfconnect.shared.dto.turf.SlotDTO;
import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final RabbitTemplate rabbitTemplate;

    @Value("${services.turf-service.url:http://localhost:8082}")
    private String turfServiceUrl;

    @Value("${services.payment-service.url:http://localhost:8084}")
    private String paymentServiceUrl;

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${spring.security.internal-token:internal-secret-token}")
    private String internalToken;

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
                .splitContribution(buildSplitContribution(slot.getPrice(), request.getSplitContribution()))
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

        // Publish booking creation event to RabbitMQ
        publishBookingEvent(savedBooking, "CREATED");

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

        // Publish booking confirmation event to RabbitMQ
        publishBookingEvent(saved, "CONFIRMED");

        return toBookingResponse(saved);
    }

    public BookingResponse cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        requireBookingOwner(booking, userId);

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

        // Publish booking cancellation event to RabbitMQ
        publishBookingEvent(saved, "CANCELLED");

        // Trigger refund if the payment was successfully charged.
        // Refund failure is non-blocking — the booking cancellation is already persisted.
        triggerRefundIfApplicable(bookingId, userId);

        return toBookingResponse(saved);
    }

    public SplitContributionResponse updateSplitContribution(String bookingId, String userId, SplitContributionRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        requireBookingOwner(booking, userId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot update split contributions for a cancelled booking.");
        }

        SplitContributionResponse splitContribution = buildSplitContribution(booking.getTotalPrice(), request);
        booking.setSplitContribution(splitContribution);
        Booking saved = bookingRepository.save(booking);
        return saved.getSplitContribution();
    }

    /**
     * Checks whether a successful payment exists for this booking and, if so,
     * calls payment-service to initiate a refund.
     *
     * Design decisions:
     * - We query payment-service via REST (internal token) to check payment status.
     * - If no payment record exists (e.g., booking was never paid), we skip silently.
     * - If payment is already in a refund state (idempotency), payment-service returns current state.
     * - Any exception here is caught and logged — the cancellation is already committed.
     */
    private void triggerRefundIfApplicable(String bookingId, String userId) {
        try {
            // 1. Check if a payment exists and is in SUCCESS state
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", internalToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String paymentCheckUrl = paymentServiceUrl + "/api/v1/payments/booking/" + bookingId;
            ResponseEntity<PaymentStatusWrapper> paymentResponse =
                    restTemplate.exchange(paymentCheckUrl, HttpMethod.GET, entity, PaymentStatusWrapper.class);

            if (paymentResponse == null || paymentResponse.getBody() == null || !paymentResponse.getBody().isSuccess()
                    || paymentResponse.getBody().getData() == null) {
                log.info("No payment record found for bookingId={}. Skipping refund.", bookingId);
                return;
            }

            String paymentStatus = String.valueOf(paymentResponse.getBody().getData().getStatus());
            if (!PaymentStatus.SUCCESS.name().equals(paymentStatus)) {
                log.info("Payment for bookingId={} is in status {}. Refund not applicable.", bookingId, paymentStatus);
                return;
            }

            // 2. Initiate refund via payment-service
            RefundRequest refundRequest = RefundRequest.builder()
                    .bookingId(bookingId)
                    .reason("Booking cancelled by user")
                    .initiatedBy(userId != null ? userId : "SYSTEM")
                    .build();

            HttpEntity<RefundRequest> refundEntity = new HttpEntity<>(refundRequest, headers);
            String refundUrl = paymentServiceUrl + "/api/v1/payments/refund";
            restTemplate.exchange(refundUrl, HttpMethod.POST, refundEntity, Void.class);
            log.info("Refund successfully initiated for bookingId={}", bookingId);

        } catch (Exception e) {
            // Refund failure is non-fatal — booking is cancelled, refund can be retried
            log.error("Failed to initiate refund for bookingId={}: {}. Manual retry may be required.",
                    bookingId, e.getMessage());
        }
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

    public List<BookingResponse> getBookingsForOwnedTurf(String turfId, String ownerId, String userRole) {
        if (!isAdminRole(userRole)) {
            requireTurfOwner(turfId, ownerId);
        }

        List<Booking> list = bookingRepository.findByTurfIdOrderByCreatedAtDesc(turfId);
        List<BookingResponse> dtos = new ArrayList<>();
        for (Booking b : list) {
            dtos.add(toBookingResponse(b));
        }
        return dtos;
    }

    private BookingResponse toBookingResponse(Booking booking) {
        UserData user = fetchUserInfo(booking.getUserId());
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .userName(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userMobileNumber(user != null ? user.getMobileNumber() : null)
                .slotId(booking.getSlotId())
                .turfId(booking.getTurfId())
                .date(booking.getDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .splitContribution(booking.getSplitContribution())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private void requireTurfOwner(String turfId, String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new ForbiddenException("Owner identity is required to view turf bookings.");
        }

        TurfData turf = fetchTurfData(turfId);
        if (turf == null || turf.getOwnerId() == null || !turf.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You can only view bookings for your own turfs.");
        }
    }

    private boolean isAdminRole(String role) {
        return "ORG_ADMIN".equals(role) || "FRANCHISE_ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    private void requireBookingOwner(Booking booking, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("User identity is required for this booking action.");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only modify your own bookings.");
        }
    }

    private SplitContributionResponse buildSplitContribution(BigDecimal totalAmount, SplitContributionRequest request) {
        if (request == null || request.getMembers() == null || request.getMembers().isEmpty()) {
            return null;
        }

        int memberCount = request.getMembers().size();
        if (memberCount > 30) {
            throw new BadRequestException("Split contributions cannot include more than 30 members.");
        }

        BigDecimal normalizedTotal = totalAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal perMember = normalizedTotal.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.DOWN);
        BigDecimal assignedTotal = BigDecimal.ZERO;
        List<SplitContributionMember> members = new ArrayList<>();

        for (int i = 0; i < memberCount; i++) {
            SplitContributionMember input = request.getMembers().get(i);
            String name = input.getName() != null ? input.getName().trim() : "";
            if (name.isBlank()) {
                throw new BadRequestException("Split member name is required.");
            }

            BigDecimal memberAmount = i == memberCount - 1
                    ? normalizedTotal.subtract(assignedTotal)
                    : perMember;
            assignedTotal = assignedTotal.add(memberAmount);

            members.add(SplitContributionMember.builder()
                    .id(input.getId() != null && !input.getId().isBlank() ? input.getId() : "member-" + (i + 1))
                    .name(name)
                    .amount(memberAmount)
                    .status(input.getStatus() != null ? input.getStatus() : SplitContributionStatus.PENDING)
                    .build());
        }

        BigDecimal collectedAmount = members.stream()
                .filter(member -> member.getStatus() == SplitContributionStatus.PAID)
                .map(SplitContributionMember::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal pendingAmount = normalizedTotal.subtract(collectedAmount).max(BigDecimal.ZERO);

        return SplitContributionResponse.builder()
                .totalAmount(normalizedTotal)
                .perMemberAmount(perMember)
                .collectedAmount(collectedAmount)
                .pendingAmount(pendingAmount)
                .memberCount(memberCount)
                .members(members)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void publishBookingEvent(Booking booking, String eventType) {
        try {
            String turfName = fetchTurfName(booking.getTurfId());
            com.turfconnect.shared.dto.event.BookingEvent event = com.turfconnect.shared.dto.event.BookingEvent.builder()
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .turfId(booking.getTurfId())
                    .turfName(turfName)
                    .date(booking.getDate())
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .totalPrice(booking.getTotalPrice())
                    .status(booking.getStatus())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(com.turfconnect.booking.config.RabbitMQConfig.BOOKING_EXCHANGE, "booking." + eventType.toLowerCase(), event);
            log.info("Successfully published booking event {} for booking {}", eventType, booking.getId());
        } catch (Exception e) {
            log.error("Failed to publish booking event", e);
        }
    }

    private String fetchTurfName(String turfId) {
        TurfData turf = fetchTurfData(turfId);
        return turf != null && turf.getName() != null ? turf.getName() : "Sports Venue";
    }

    private TurfData fetchTurfData(String turfId) {
        try {
            String url = turfServiceUrl + "/api/v1/turfs/" + turfId;
            ResponseEntity<TurfResponseWrapper> response = restTemplate.getForEntity(url, TurfResponseWrapper.class);
            if (response != null && response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch turf data for turfId: " + turfId, e);
        }
        return null;
    }

    private UserData fetchUserInfo(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", internalToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = authServiceUrl + "/api/v1/auth/users/internal/" + userId;
            ResponseEntity<UserResponseWrapper> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, UserResponseWrapper.class);
            if (response != null && response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user info for userId: " + userId, e);
        }
        return null;
    }

    // Static helper inner class to handle deserialization of ApiResponse wrapped SlotDTO
    @Data
    public static class SlotResponseWrapper {
        private boolean success;
        private SlotDTO data;
        private String message;
    }

    @Data
    public static class TurfResponseWrapper {
        private boolean success;
        private TurfData data;
        private String message;
    }

    @Data
    public static class TurfData {
        private String id;
        private String ownerId;
        private String name;
    }

    @Data
    public static class UserResponseWrapper {
        private boolean success;
        private UserData data;
        private String message;
    }

    @Data
    public static class UserData {
        private String id;
        private String email;
        private String name;
        private String mobileNumber;
        private String role;
    }

    // Wrapper for deserializing payment status response from payment-service
    @Data
    public static class PaymentStatusWrapper {
        private boolean success;
        private PaymentData data;
        private String message;
    }

    @Data
    public static class PaymentData {
        private String id;
        private PaymentStatus status;
    }
}

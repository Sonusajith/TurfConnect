package com.turfconnect.booking.service;

import com.turfconnect.booking.model.Booking;
import com.turfconnect.booking.repository.BookingRepository;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.shared.dto.booking.BookingStatus;
import com.turfconnect.shared.dto.booking.SplitContributionMember;
import com.turfconnect.shared.dto.booking.SplitContributionRequest;
import com.turfconnect.shared.dto.booking.SplitContributionResponse;
import com.turfconnect.shared.dto.booking.SplitContributionStatus;
import com.turfconnect.shared.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceSplitTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RedisLockService redisLockService;

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = Booking.builder()
                .id("booking-1")
                .userId("user-1")
                .slotId("slot-1")
                .turfId("turf-1")
                .date(LocalDate.of(2026, 7, 20))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .totalPrice(new BigDecimal("100.00"))
                .status(BookingStatus.CONFIRMED)
                .lockToken("lock-token")
                .build();

        ReflectionTestUtils.setField(bookingService, "turfServiceUrl", "http://turf-service");
        ReflectionTestUtils.setField(bookingService, "authServiceUrl", "http://auth-service");
        ReflectionTestUtils.setField(bookingService, "internalToken", "test-internal-token");
    }

    @Test
    void updateSplitContribution_savesNormalizedMembersAndTotals() {
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SplitContributionRequest request = SplitContributionRequest.builder()
                .members(List.of(
                        member("member-1", "You", SplitContributionStatus.PAID),
                        member("member-2", "Asha", SplitContributionStatus.PENDING),
                        member("member-3", "Rohit", SplitContributionStatus.PENDING)
                ))
                .build();

        SplitContributionResponse response = bookingService.updateSplitContribution("booking-1", "user-1", request);

        assertNotNull(response);
        assertEquals(3, response.getMemberCount());
        assertEquals(new BigDecimal("33.33"), response.getMembers().get(0).getAmount());
        assertEquals(new BigDecimal("33.34"), response.getMembers().get(2).getAmount());
        assertEquals(new BigDecimal("33.33"), response.getCollectedAmount());
        assertEquals(new BigDecimal("66.67"), response.getPendingAmount());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateSplitContribution_rejectsNonOwner() {
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        SplitContributionRequest request = SplitContributionRequest.builder()
                .members(List.of(member("member-1", "You", SplitContributionStatus.PAID)))
                .build();

        assertThrows(ForbiddenException.class,
                () -> bookingService.updateSplitContribution("booking-1", "other-user", request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_allowsOwnerAndReturnsCancelledBooking() {
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking("booking-1", "user-1");

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        verify(redisLockService).releaseLock("lock:slot:slot-1", "lock-token");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getBookingsForOwnedTurf_allowsOwnerAndIncludesCustomerInfo() {
        when(restTemplate.getForEntity(eq("http://turf-service/api/v1/turfs/turf-1"), eq(BookingService.TurfResponseWrapper.class)))
                .thenReturn(ResponseEntity.ok(turfWrapper("owner-1")));
        when(restTemplate.exchange(
                eq("http://auth-service/api/v1/auth/users/internal/user-1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BookingService.UserResponseWrapper.class)))
                .thenReturn(ResponseEntity.ok(userWrapper("Saif Player", "saif.player@turfconnect.test")));
        when(bookingRepository.findByTurfIdOrderByCreatedAtDesc("turf-1")).thenReturn(List.of(booking));

        List<BookingResponse> response = bookingService.getBookingsForOwnedTurf("turf-1", "owner-1", "TURF_OWNER");

        assertEquals(1, response.size());
        assertEquals("Saif Player", response.get(0).getUserName());
        assertEquals("saif.player@turfconnect.test", response.get(0).getUserEmail());
        verify(bookingRepository).findByTurfIdOrderByCreatedAtDesc("turf-1");
    }

    @Test
    void getBookingsForOwnedTurf_rejectsDifferentOwner() {
        when(restTemplate.getForEntity(eq("http://turf-service/api/v1/turfs/turf-1"), eq(BookingService.TurfResponseWrapper.class)))
                .thenReturn(ResponseEntity.ok(turfWrapper("owner-1")));

        assertThrows(ForbiddenException.class,
                () -> bookingService.getBookingsForOwnedTurf("turf-1", "other-owner", "TURF_OWNER"));
        verify(bookingRepository, never()).findByTurfIdOrderByCreatedAtDesc("turf-1");
    }

    private SplitContributionMember member(String id, String name, SplitContributionStatus status) {
        return SplitContributionMember.builder()
                .id(id)
                .name(name)
                .amount(BigDecimal.ONE)
                .status(status)
                .build();
    }

    private BookingService.TurfResponseWrapper turfWrapper(String ownerId) {
        BookingService.TurfData turf = new BookingService.TurfData();
        turf.setId("turf-1");
        turf.setName("GreenField Arena");
        turf.setOwnerId(ownerId);

        BookingService.TurfResponseWrapper wrapper = new BookingService.TurfResponseWrapper();
        wrapper.setSuccess(true);
        wrapper.setData(turf);
        return wrapper;
    }

    private BookingService.UserResponseWrapper userWrapper(String name, String email) {
        BookingService.UserData user = new BookingService.UserData();
        user.setId("user-1");
        user.setName(name);
        user.setEmail(email);

        BookingService.UserResponseWrapper wrapper = new BookingService.UserResponseWrapper();
        wrapper.setSuccess(true);
        wrapper.setData(user);
        return wrapper;
    }
}

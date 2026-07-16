package com.turfconnect.booking.service;

import com.turfconnect.booking.model.Booking;
import com.turfconnect.booking.repository.BookingRepository;
import com.turfconnect.shared.dto.booking.BookingCreateRequest;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.shared.dto.booking.BookingStatus;
import com.turfconnect.shared.dto.turf.SlotDTO;
import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingServiceConcurrencyTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RedisLockService redisLockService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingService bookingService;

    private SlotDTO slot;
    private BookingCreateRequest request;
    private final String userId = "user-789";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingService, "turfServiceUrl", "http://localhost:8082");

        slot = SlotDTO.builder()
                .id("slot-555")
                .turfId("turf-10")
                .date(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .price(new BigDecimal("120.00"))
                .status(SlotStatus.AVAILABLE)
                .build();

        request = BookingCreateRequest.builder()
                .slotId("slot-555")
                .totalPrice(new BigDecimal("120.00"))
                .build();
    }

    @Test
    void createBooking_ConcurrentRequests_ExclusivityTest() throws InterruptedException, ExecutionException {
        // Mock internal REST call to turf-service
        BookingService.SlotResponseWrapper wrapper = new BookingService.SlotResponseWrapper();
        wrapper.setSuccess(true);
        wrapper.setData(slot);
        
        when(restTemplate.getForEntity(anyString(), eq(BookingService.SlotResponseWrapper.class)))
                .thenReturn(ResponseEntity.ok(wrapper));

        // Mock lock acquisition: only first thread gets the lock
        AtomicBoolean lockHeld = new AtomicBoolean(false);
        when(redisLockService.acquireLock(eq("lock:slot:slot-555"), anyString(), eq(300000L)))
                .thenAnswer(invocation -> !lockHeld.getAndSet(true));

        // Mock repository save
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId("booking-generated-id");
            return b;
        });

        // Mock RestTemplate PUT update status call
        doNothing().when(restTemplate).put(anyString(), eq(null));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        List<Callable<BookingResponse>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String threadUser = "user-" + i;
            tasks.add(() -> {
                barrier.await(); // Synchronize starts
                return bookingService.createBooking(request, threadUser);
            });
        }

        List<Future<BookingResponse>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        int successCount = 0;
        int failureCount = 0;

        for (Future<BookingResponse> future : futures) {
            try {
                BookingResponse res = future.get();
                assertNotNull(res);
                assertEquals(BookingStatus.PENDING, res.getStatus());
                assertEquals("slot-555", res.getSlotId());
                successCount++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof BadRequestException) {
                    assertTrue(e.getCause().getMessage().contains("locked by another user"));
                    failureCount++;
                } else {
                    fail("Unexpected exception: " + e.getCause());
                }
            }
        }

        // Verify that exactly 1 thread succeeded and the rest (9) failed
        assertEquals(1, successCount);
        assertEquals(threadCount - 1, failureCount);

        // Verify save was only called once
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(restTemplate, times(1)).put(anyString(), eq(null));
    }
}

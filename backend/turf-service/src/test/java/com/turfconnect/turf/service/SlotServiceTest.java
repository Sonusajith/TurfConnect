package com.turfconnect.turf.service;

import com.turfconnect.shared.dto.turf.SlotDTO;
import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.turf.dto.TurfCreateRequest;
import com.turfconnect.turf.dto.TurfUpdateRequest;
import com.turfconnect.turf.model.Turf;
import com.turfconnect.turf.model.Slot;
import com.turfconnect.turf.repository.TurfRepository;
import com.turfconnect.turf.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlotServiceTest {

    @Mock
    private TurfRepository turfRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private com.turfconnect.turf.controller.SlotBroadcaster slotBroadcaster;

    @InjectMocks
    private TurfService turfService;

    private Turf turf;
    private final String ownerId = "owner-123";

    @BeforeEach
    void setUp() {
        turf = Turf.builder()
                .id("turf-1")
                .ownerId(ownerId)
                .name("Green Arena")
                .city("New York")
                .hourlyRate(new BigDecimal("100.00"))
                .sportTypes(List.of("Football"))
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(22, 0))
                .slotDurationMinutes(60)
                .availableDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"))
                .deleted(false)
                .build();
    }

    @Test
    void createTurf_InvalidDuration_ThrowsBadRequest() {
        TurfCreateRequest req = TurfCreateRequest.builder()
                .name("Green Arena")
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(22, 0))
                .slotDurationMinutes(45) // 14 hours is 840 minutes, 45 does not divide 840 evenly
                .hourlyRate(new BigDecimal("100.00"))
                .build();

        assertThrows(BadRequestException.class, () -> turfService.createTurf(req, ownerId));
    }

    @Test
    void createTurf_CloseBeforeOpen_ThrowsBadRequest() {
        TurfCreateRequest req = TurfCreateRequest.builder()
                .name("Green Arena")
                .openTime(LocalTime.of(22, 0))
                .closeTime(LocalTime.of(8, 0))
                .slotDurationMinutes(60)
                .hourlyRate(new BigDecimal("100.00"))
                .build();

        assertThrows(BadRequestException.class, () -> turfService.createTurf(req, ownerId));
    }

    @Test
    void updateTurf_InvalidSlotConfig_ThrowsBadRequest() {
        TurfUpdateRequest req = TurfUpdateRequest.builder()
                .slotDurationMinutes(90) // 14 hours is 840 mins, 90 does not divide 840 evenly
                .build();

        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));

        assertThrows(BadRequestException.class, () -> turfService.updateTurf("turf-1", req, ownerId));
    }

    @Test
    void getSlots_LazyGeneration_Success() {
        LocalDate date = LocalDate.of(2026, 7, 20); // A Monday
        
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));
        when(slotRepository.existsByTurfIdAndDate("turf-1", date)).thenReturn(false);
        
        List<Slot> savedSlots = new ArrayList<>();
        when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Slot> arg = invocation.getArgument(0);
            savedSlots.addAll(arg);
            return arg;
        });
        
        when(slotRepository.findByTurfIdAndDate("turf-1", date)).thenAnswer(invocation -> savedSlots);

        List<SlotDTO> slots = turfService.getSlots("turf-1", date);

        // 8:00 to 22:00 = 14 hours = 14 slots of 60 mins
        assertEquals(14, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).getStartTime());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getEndTime());
        assertEquals(LocalTime.of(21, 0), slots.get(13).getStartTime());
        assertEquals(LocalTime.of(22, 0), slots.get(13).getEndTime());
        verify(slotRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getSlots_ClosedDay_ReturnsEmptyList() {
        // Turf only open MON-SUN normally, but let's change availableDays to only MONDAY
        turf.setAvailableDays(List.of("MONDAY"));
        LocalDate date = LocalDate.of(2026, 7, 21); // July 21, 2026 is a Tuesday
        
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));

        List<SlotDTO> slots = turfService.getSlots("turf-1", date);
        assertTrue(slots.isEmpty());
        verify(slotRepository, never()).existsByTurfIdAndDate(anyString(), any(LocalDate.class));
    }

    @Test
    void getSlots_ConcurrencySafety_OnlyOneGenerates() throws InterruptedException, ExecutionException {
        LocalDate date = LocalDate.of(2026, 7, 20);
        
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));
        
        // Mock in-memory database storage
        List<Slot> dbSlots = new CopyOnWriteArrayList<>();
        
        when(slotRepository.existsByTurfIdAndDate(eq("turf-1"), eq(date)))
                .thenAnswer(invocation -> !dbSlots.isEmpty());
        
        AtomicInteger saveAllCalls = new AtomicInteger(0);
        when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> {
            saveAllCalls.incrementAndGet();
            List<Slot> slotsToSave = invocation.getArgument(0);
            
            // Simulate Mongo unique index check
            if (!dbSlots.isEmpty()) {
                throw new DuplicateKeyException("Duplicate key error on compound index");
            }
            dbSlots.addAll(slotsToSave);
            return slotsToSave;
        });
        
        when(slotRepository.findByTurfIdAndDate("turf-1", date))
                .thenAnswer(invocation -> new ArrayList<>(dbSlots));

        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        List<Callable<List<SlotDTO>>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                barrier.await(); // Synchronize thread starts
                return turfService.getSlots("turf-1", date);
            });
        }

        List<Future<List<SlotDTO>>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        for (Future<List<SlotDTO>> future : futures) {
            List<SlotDTO> slots = future.get();
            assertEquals(14, slots.size()); // All threads get correct data
        }

        // Even with 8 threads triggering lazy generation concurrently,
        // local synchronization and duplicate key catches ensure saveAll is only successful once.
        // It could be called more than once if check occurs before lock, but unique index catches it.
        assertTrue(saveAllCalls.get() >= 1); 
        assertEquals(14, dbSlots.size());
    }

    @Test
    void updateSlotStatus_Success() {
        Slot slot = Slot.builder()
                .id("slot-1")
                .turfId("turf-1")
                .date(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findById("slot-1")).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SlotDTO updated = turfService.updateSlotStatus("slot-1", SlotStatus.BOOKED, "booking-999");

        assertNotNull(updated);
        assertEquals(SlotStatus.BOOKED, updated.getStatus());
        assertEquals("booking-999", updated.getBookingId());
        verify(slotRepository).save(any(Slot.class));
    }
}

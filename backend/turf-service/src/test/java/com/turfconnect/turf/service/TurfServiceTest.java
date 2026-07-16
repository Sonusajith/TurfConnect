package com.turfconnect.turf.service;

import com.turfconnect.shared.dto.PageResponse;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import com.turfconnect.turf.dto.TurfCreateRequest;
import com.turfconnect.turf.dto.TurfResponse;
import com.turfconnect.turf.dto.TurfSearchCriteria;
import com.turfconnect.turf.dto.TurfUpdateRequest;
import com.turfconnect.turf.mapper.TurfMapper;
import com.turfconnect.turf.model.Turf;
import com.turfconnect.turf.repository.TurfRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TurfServiceTest {

    @Mock
    private TurfRepository turfRepository;

    @Mock
    private com.turfconnect.turf.repository.SlotRepository slotRepository;

    @Spy
    private TurfMapper turfMapper = new TurfMapper();

    @Mock
    private com.turfconnect.turf.controller.SlotBroadcaster slotBroadcaster;

    @Mock
    private TurfCacheService turfCacheService;

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
                .deleted(false)
                .build();
    }

    @Test
    void createTurf_Success() {
        TurfCreateRequest req = TurfCreateRequest.builder()
                .name("Green Arena")
                .city("New York")
                .hourlyRate(new BigDecimal("100.00"))
                .sportTypes(List.of("Football"))
                .latitude(40.7128)
                .longitude(-74.0060)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(22, 0))
                .slotDurationMinutes(60)
                .build();

        when(turfRepository.save(any(Turf.class))).thenReturn(turf);

        TurfResponse response = turfService.createTurf(req, ownerId);

        assertNotNull(response);
        assertEquals("Green Arena", response.getName());
        assertEquals(ownerId, response.getOwnerId());
    }

    @Test
    void updateTurf_Success() {
        TurfUpdateRequest req = TurfUpdateRequest.builder()
                .name("Updated Arena")
                .build();

        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));
        when(turfRepository.save(any(Turf.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurfResponse response = turfService.updateTurf("turf-1", req, ownerId);

        assertEquals("Updated Arena", response.getName());
        verify(turfRepository).save(any(Turf.class));
    }

    @Test
    void updateTurf_Forbidden_NotOwner() {
        TurfUpdateRequest req = TurfUpdateRequest.builder().name("Updated Arena").build();

        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));

        assertThrows(ForbiddenException.class, () -> turfService.updateTurf("turf-1", req, "other-owner"));
        verify(turfRepository, never()).save(any(Turf.class));
    }

    @Test
    void deleteTurf_Success_SoftDelete() {
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));

        turfService.deleteTurf("turf-1", ownerId);

        assertTrue(turf.isDeleted());
        verify(turfRepository).save(turf);
    }

    @Test
    void deleteTurf_Forbidden_NotOwner() {
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));

        assertThrows(ForbiddenException.class, () -> turfService.deleteTurf("turf-1", "other-owner"));
        assertFalse(turf.isDeleted());
        verify(turfRepository, never()).save(any());
    }

    @Test
    void getTurfById_Success() {
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));

        TurfResponse response = turfService.getTurfById("turf-1");

        assertNotNull(response);
        assertEquals("turf-1", response.getId());
    }

    @Test
    void searchTurfs_Success() {
        TurfSearchCriteria criteria = new TurfSearchCriteria();
        criteria.setCity("New York");
        criteria.setPage(0);
        criteria.setSize(10);
        criteria.setSortBy("createdAt");
        criteria.setSortDirection("desc");

        Page<Turf> turfPage = new PageImpl<>(List.of(turf), PageRequest.of(0, 10), 1);
        
        when(turfRepository.searchTurfs(
                eq("New York"), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(5000.0),
                any(Pageable.class)
        )).thenReturn(turfPage);

        PageResponse<TurfResponse> response = turfService.searchTurfs(criteria);

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals("Green Arena", response.getContent().get(0).getName());
    }

    @Test
    void updateTurfRating_Success() {
        when(turfRepository.findByIdAndDeletedFalse("turf-1")).thenReturn(Optional.of(turf));
        when(turfRepository.save(any(Turf.class))).thenReturn(turf);

        assertDoesNotThrow(() -> turfService.updateTurfRating("turf-1", 4.7));

        assertEquals(4.7, turf.getAverageRating());
        verify(turfRepository, times(1)).save(turf);
    }
}

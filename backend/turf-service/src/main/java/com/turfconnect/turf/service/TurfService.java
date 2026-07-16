package com.turfconnect.turf.service;

import com.turfconnect.shared.dto.PageResponse;
import com.turfconnect.shared.dto.turf.SlotDTO;
import com.turfconnect.shared.dto.turf.SlotStatus;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import com.turfconnect.turf.model.Slot;
import com.turfconnect.turf.repository.SlotRepository;
import org.springframework.dao.DuplicateKeyException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.turfconnect.turf.dto.TurfCreateRequest;
import com.turfconnect.turf.dto.TurfResponse;
import com.turfconnect.turf.dto.TurfSearchCriteria;
import com.turfconnect.turf.dto.TurfUpdateRequest;
import com.turfconnect.turf.mapper.TurfMapper;
import com.turfconnect.turf.model.Turf;
import com.turfconnect.turf.repository.TurfRepository;
import com.turfconnect.turf.controller.SlotBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TurfService {

    private final TurfRepository turfRepository;
    private final SlotRepository slotRepository;
    private final TurfMapper turfMapper;
    private final SlotBroadcaster slotBroadcaster;

    public TurfResponse createTurf(TurfCreateRequest request, String ownerId) {
        validateSlotConfiguration(request.getOpenTime(), request.getCloseTime(), request.getSlotDurationMinutes());
        Turf turf = turfMapper.toEntity(request, ownerId);
        Turf savedTurf = turfRepository.save(turf);
        return turfMapper.toResponse(savedTurf);
    }

    public TurfResponse updateTurf(String id, TurfUpdateRequest request, String ownerId) {
        Turf turf = turfRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + id));

        if (!turf.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You do not have permission to update this turf.");
        }

        // Validate final slot configuration state
        LocalTime openTime = request.getOpenTime() != null ? request.getOpenTime() : turf.getOpenTime();
        LocalTime closeTime = request.getCloseTime() != null ? request.getCloseTime() : turf.getCloseTime();
        Integer duration = request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : turf.getSlotDurationMinutes();
        validateSlotConfiguration(openTime, closeTime, duration);

        // Apply updates
        if (request.getName() != null) turf.setName(request.getName());
        if (request.getDescription() != null) turf.setDescription(request.getDescription());
        if (request.getSportTypes() != null) turf.setSportTypes(request.getSportTypes());
        if (request.getAddress() != null) turf.setAddress(request.getAddress());
        if (request.getCity() != null) turf.setCity(request.getCity());
        if (request.getState() != null) turf.setState(request.getState());
        if (request.getCountry() != null) turf.setCountry(request.getCountry());
        if (request.getPostalCode() != null) turf.setPostalCode(request.getPostalCode());
        
        if (request.getLongitude() != null && request.getLatitude() != null) {
            turf.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }
        
        if (request.getTimezone() != null) turf.setTimezone(request.getTimezone());
        if (request.getHourlyRate() != null) turf.setHourlyRate(request.getHourlyRate());
        if (request.getCurrency() != null) turf.setCurrency(request.getCurrency());
        if (request.getAmenities() != null) turf.setAmenities(request.getAmenities());
        
        if (request.getOpenTime() != null) turf.setOpenTime(request.getOpenTime());
        if (request.getCloseTime() != null) turf.setCloseTime(request.getCloseTime());
        if (request.getSlotDurationMinutes() != null) turf.setSlotDurationMinutes(request.getSlotDurationMinutes());
        
        if (request.getAvailableDays() != null) turf.setAvailableDays(request.getAvailableDays());
        if (request.getContactNumber() != null) turf.setContactNumber(request.getContactNumber());
        if (request.getEmail() != null) turf.setEmail(request.getEmail());
        if (request.getCapacity() != null) turf.setCapacity(request.getCapacity());
        if (request.getSurfaceType() != null) turf.setSurfaceType(request.getSurfaceType());
        if (request.getIndoorOrOutdoor() != null) turf.setIndoorOrOutdoor(request.getIndoorOrOutdoor());
        
        if (request.getFloodlightsAvailable() != null) turf.setFloodlightsAvailable(request.getFloodlightsAvailable());
        if (request.getParkingAvailable() != null) turf.setParkingAvailable(request.getParkingAvailable());
        if (request.getChangingRoomsAvailable() != null) turf.setChangingRoomsAvailable(request.getChangingRoomsAvailable());
        if (request.getWashroomsAvailable() != null) turf.setWashroomsAvailable(request.getWashroomsAvailable());
        if (request.getDrinkingWaterAvailable() != null) turf.setDrinkingWaterAvailable(request.getDrinkingWaterAvailable());
        if (request.getEquipmentRentalAvailable() != null) turf.setEquipmentRentalAvailable(request.getEquipmentRentalAvailable());
        if (request.getFoodAvailable() != null) turf.setFoodAvailable(request.getFoodAvailable());
        if (request.getCoachingAvailable() != null) turf.setCoachingAvailable(request.getCoachingAvailable());
        
        if (request.getStatus() != null) turf.setStatus(request.getStatus());

        Turf updatedTurf = turfRepository.save(turf);
        return turfMapper.toResponse(updatedTurf);
    }

    public void deleteTurf(String id, String ownerId) {
        Turf turf = turfRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + id));

        if (!turf.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You do not have permission to delete this turf.");
        }

        turf.setDeleted(true);
        turf.setUpdatedAt(LocalDateTime.now());
        turfRepository.save(turf);
    }

    public TurfResponse getTurfById(String id) {
        Turf turf = turfRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + id));
        return turfMapper.toResponse(turf);
    }

    public PageResponse<TurfResponse> searchTurfs(TurfSearchCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.fromString(criteria.getSortDirection()), criteria.getSortBy());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        Page<Turf> page = turfRepository.searchTurfs(
                criteria.getCity(),
                criteria.getSport(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating(),
                criteria.getSurfaceType(),
                criteria.getIndoorOrOutdoor(),
                criteria.getFloodlights(),
                criteria.getLongitude(),
                criteria.getLatitude(),
                criteria.getRadiusInMeters() != null ? criteria.getRadiusInMeters() : 5000.0,
                pageable
        );
        
        Page<TurfResponse> responsePage = page.map(turfMapper::toResponse);
        return PageResponse.<TurfResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();
    }

    public PageResponse<TurfResponse> getMyTurfs(String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Turf> turfs = turfRepository.findByOwnerIdAndDeletedFalse(ownerId, pageable);
        Page<TurfResponse> responsePage = turfs.map(turfMapper::toResponse);
        return PageResponse.<TurfResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();
    }

    private void validateSlotConfiguration(LocalTime openTime, LocalTime closeTime, Integer slotDurationMinutes) {
        if (slotDurationMinutes == null || slotDurationMinutes <= 0) {
            throw new BadRequestException("Slot duration must be positive");
        }
        if (openTime == null || closeTime == null) {
            throw new BadRequestException("Open time and close time must be defined");
        }
        if (closeTime.isBefore(openTime) || closeTime.equals(openTime)) {
            throw new BadRequestException("Close time must be after open time");
        }
        long totalMinutes = java.time.temporal.ChronoUnit.MINUTES.between(openTime, closeTime);
        if (totalMinutes % slotDurationMinutes != 0) {
            throw new BadRequestException("Slot duration (" + slotDurationMinutes + " minutes) must evenly divide operating hours (total " + totalMinutes + " minutes)");
        }
    }

    public List<SlotDTO> getSlots(String turfId, LocalDate date) {
        Turf turf = turfRepository.findByIdAndDeletedFalse(turfId)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + turfId));

        if (!isDayAvailable(turf, date)) {
            return Collections.emptyList();
        }

        if (!slotRepository.existsByTurfIdAndDate(turfId, date)) {
            // JVM-level lock to prevent concurrent generation on the same node
            synchronized (turfId.intern()) {
                if (!slotRepository.existsByTurfIdAndDate(turfId, date)) {
                    List<Slot> slots = new ArrayList<>();
                    LocalTime current = turf.getOpenTime();
                    LocalTime end = turf.getCloseTime();
                    int duration = turf.getSlotDurationMinutes();

                    while (current.plusMinutes(duration).isBefore(end) || current.plusMinutes(duration).equals(end)) {
                        LocalTime slotEnd = current.plusMinutes(duration);
                        Slot slot = Slot.builder()
                                .turfId(turfId)
                                .date(date)
                                .startTime(current)
                                .endTime(slotEnd)
                                .price(turf.getHourlyRate())
                                .status(SlotStatus.AVAILABLE)
                                .build();
                        slots.add(slot);
                        current = slotEnd;
                    }

                    try {
                        slotRepository.saveAll(slots);
                    } catch (DuplicateKeyException e) {
                        // Another thread/instance generated them concurrently, safe to ignore
                    }
                }
            }
        }

        List<Slot> slots = slotRepository.findByTurfIdAndDate(turfId, date);
        slots.sort(Comparator.comparing(Slot::getStartTime));

        List<SlotDTO> dtos = new ArrayList<>();
        for (Slot s : slots) {
            dtos.add(toSlotDTO(s));
        }
        return dtos;
    }

    public void generateSlotsForDateRange(String turfId, LocalDate startDate, LocalDate endDate, String ownerId) {
        Turf turf = turfRepository.findByIdAndDeletedFalse(turfId)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + turfId));

        if (!turf.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You do not have permission to generate slots for this turf.");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isDayAvailable(turf, current) && !slotRepository.existsByTurfIdAndDate(turfId, current)) {
                final LocalDate dateToGen = current;
                synchronized (turfId.intern()) {
                    if (!slotRepository.existsByTurfIdAndDate(turfId, dateToGen)) {
                        List<Slot> slots = new ArrayList<>();
                        LocalTime timeCursor = turf.getOpenTime();
                        LocalTime end = turf.getCloseTime();
                        int duration = turf.getSlotDurationMinutes();

                        while (timeCursor.plusMinutes(duration).isBefore(end) || timeCursor.plusMinutes(duration).equals(end)) {
                            LocalTime slotEnd = timeCursor.plusMinutes(duration);
                            Slot slot = Slot.builder()
                                    .turfId(turfId)
                                    .date(dateToGen)
                                    .startTime(timeCursor)
                                    .endTime(slotEnd)
                                    .price(turf.getHourlyRate())
                                    .status(SlotStatus.AVAILABLE)
                                    .build();
                            slots.add(slot);
                            timeCursor = slotEnd;
                        }

                        try {
                            slotRepository.saveAll(slots);
                        } catch (DuplicateKeyException e) {
                            // Concurrency safeguard
                        }
                    }
                }
            }
            current = current.plusDays(1);
        }
    }

    public SlotDTO updateSlotStatus(String slotId, SlotStatus status, String bookingId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));

        slot.setStatus(status);
        slot.setBookingId(bookingId);
        Slot saved = slotRepository.save(slot);
        SlotDTO updated = toSlotDTO(saved);

        // Broadcast real-time slot status change to all WebSocket subscribers
        // Frontend subscribes to /topic/slots/{turfId}/{date}
        slotBroadcaster.broadcastSlotUpdate(updated);

        return updated;
    }

    private boolean isDayAvailable(Turf turf, LocalDate date) {
        if (turf.getAvailableDays() == null || turf.getAvailableDays().isEmpty()) {
            return true; // If no days specified, assume open everyday
        }
        String dayName = date.getDayOfWeek().name(); // e.g. MONDAY
        for (String openDay : turf.getAvailableDays()) {
            if (dayName.equalsIgnoreCase(openDay) || 
                (dayName.length() >= 3 && openDay.length() >= 3 && dayName.substring(0, 3).equalsIgnoreCase(openDay.substring(0, 3)))) {
                return true;
            }
        }
        return false;
    }

    public SlotDTO getSlotById(String slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        return toSlotDTO(slot);
    }

    private SlotDTO toSlotDTO(Slot slot) {
        return SlotDTO.builder()
                .id(slot.getId())
                .turfId(slot.getTurfId())
                .date(slot.getDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .price(slot.getPrice())
                .status(slot.getStatus())
                .bookingId(slot.getBookingId())
                .build();
    }

    public void updateTurfRating(String turfId, Double averageRating) {
        Turf turf = turfRepository.findByIdAndDeletedFalse(turfId)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + turfId));
        
        turf.setAverageRating(averageRating);
        turfRepository.save(turf);

        // TODO: PLACEHOLDER FOR REDIS CACHE EVICTION (Module 12 Caching)
        // Once Redis is set up in Module 12, evict cache key on update:
        // redisTemplate.delete("cache:turf:" + turfId);
        // redisTemplate.keys("cache:turfs:*").forEach(redisTemplate::delete);
    }
}

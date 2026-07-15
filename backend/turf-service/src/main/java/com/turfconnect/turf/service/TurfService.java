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
    private final TurfMapper turfMapper;

    public TurfResponse createTurf(TurfCreateRequest request, String ownerId) {
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
        if (request.getOperatingHours() != null) turf.setOperatingHours(request.getOperatingHours());
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
}

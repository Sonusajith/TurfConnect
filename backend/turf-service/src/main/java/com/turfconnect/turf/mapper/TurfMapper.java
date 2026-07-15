package com.turfconnect.turf.mapper;

import com.turfconnect.turf.dto.TurfCreateRequest;
import com.turfconnect.turf.dto.TurfResponse;
import com.turfconnect.turf.model.Turf;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class TurfMapper {

    public Turf toEntity(TurfCreateRequest request, String ownerId) {
        GeoJsonPoint location = new GeoJsonPoint(request.getLongitude(), request.getLatitude());
        
        return Turf.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .sportTypes(request.getSportTypes())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .location(location)
                .timezone(request.getTimezone())
                .hourlyRate(request.getHourlyRate())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .amenities(request.getAmenities() != null ? request.getAmenities() : Collections.emptyList())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .slotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 60)
                .availableDays(request.getAvailableDays())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .capacity(request.getCapacity())
                .surfaceType(request.getSurfaceType())
                .indoorOrOutdoor(request.getIndoorOrOutdoor())
                .floodlightsAvailable(request.isFloodlightsAvailable())
                .parkingAvailable(request.isParkingAvailable())
                .changingRoomsAvailable(request.isChangingRoomsAvailable())
                .washroomsAvailable(request.isWashroomsAvailable())
                .drinkingWaterAvailable(request.isDrinkingWaterAvailable())
                .equipmentRentalAvailable(request.isEquipmentRentalAvailable())
                .foodAvailable(request.isFoodAvailable())
                .coachingAvailable(request.isCoachingAvailable())
                .status("PENDING_APPROVAL") // Initial status before manual review
                .deleted(false)
                .averageRating(0.0)
                .totalReviews(0)
                .bookingCount(0)
                .build();
    }

    public TurfResponse toResponse(Turf turf) {
        return TurfResponse.builder()
                .id(turf.getId())
                .ownerId(turf.getOwnerId())
                .name(turf.getName())
                .description(turf.getDescription())
                .sportTypes(turf.getSportTypes())
                .address(turf.getAddress())
                .city(turf.getCity())
                .state(turf.getState())
                .country(turf.getCountry())
                .postalCode(turf.getPostalCode())
                .longitude(turf.getLocation() != null ? turf.getLocation().getX() : null)
                .latitude(turf.getLocation() != null ? turf.getLocation().getY() : null)
                .timezone(turf.getTimezone())
                .hourlyRate(turf.getHourlyRate())
                .currency(turf.getCurrency())
                .amenities(turf.getAmenities())
                .images(turf.getImages())
                .coverImage(turf.getCoverImage())
                .openTime(turf.getOpenTime())
                .closeTime(turf.getCloseTime())
                .slotDurationMinutes(turf.getSlotDurationMinutes())
                .availableDays(turf.getAvailableDays())
                .contactNumber(turf.getContactNumber())
                .email(turf.getEmail())
                .capacity(turf.getCapacity())
                .surfaceType(turf.getSurfaceType())
                .indoorOrOutdoor(turf.getIndoorOrOutdoor())
                .floodlightsAvailable(turf.isFloodlightsAvailable())
                .parkingAvailable(turf.isParkingAvailable())
                .changingRoomsAvailable(turf.isChangingRoomsAvailable())
                .washroomsAvailable(turf.isWashroomsAvailable())
                .drinkingWaterAvailable(turf.isDrinkingWaterAvailable())
                .equipmentRentalAvailable(turf.isEquipmentRentalAvailable())
                .foodAvailable(turf.isFoodAvailable())
                .coachingAvailable(turf.isCoachingAvailable())
                .averageRating(turf.getAverageRating())
                .totalReviews(turf.getTotalReviews())
                .status(turf.getStatus())
                .createdAt(turf.getCreatedAt())
                .updatedAt(turf.getUpdatedAt())
                .build();
    }
}

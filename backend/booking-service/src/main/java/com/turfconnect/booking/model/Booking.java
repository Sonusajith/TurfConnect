package com.turfconnect.booking.model;

import com.turfconnect.shared.dto.booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String slotId;

    private String turfId;
    
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private BigDecimal totalPrice;

    @Indexed
    private BookingStatus status;

    private String lockToken; // Stores the UUID generated for the Redis lock

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

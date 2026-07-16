package com.turfconnect.shared.dto.event;

import com.turfconnect.shared.dto.booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Event published when a booking changes state (CREATED, CONFIRMED, CANCELLED).
 * Delivered via RabbitMQ to notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private String bookingId;
    private String userId;
    private String turfId;
    private String turfName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String eventType; // "CREATED", "CONFIRMED", "CANCELLED"
    private LocalDateTime timestamp;
}

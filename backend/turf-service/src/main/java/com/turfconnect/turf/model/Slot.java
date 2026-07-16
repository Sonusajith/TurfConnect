package com.turfconnect.turf.model;

import com.turfconnect.shared.dto.turf.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
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
@Document(collection = "slots")
@CompoundIndexes({
    @CompoundIndex(name = "turf_date_start_time_idx", def = "{'turfId': 1, 'date': 1, 'startTime': 1}", unique = true),
    @CompoundIndex(name = "turf_date_idx", def = "{'turfId': 1, 'date': 1}")
})
public class Slot {

    @Id
    private String id;

    @Indexed
    private String turfId;

    @Indexed
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    private BigDecimal price;

    @Indexed
    private SlotStatus status;

    private String bookingId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

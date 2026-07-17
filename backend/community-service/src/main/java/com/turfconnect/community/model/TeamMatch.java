package com.turfconnect.community.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "matches")
public class TeamMatch {

    @Id
    private String id;

    @Indexed
    private String homeTeamId;
    
    @Indexed
    private String awayTeamId;

    @Indexed
    private String bookingId;

    private String turfId;
    
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private MatchType matchType;
    private String sportType;
    private MatchStatus status;

    private String winnerTeamId;
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

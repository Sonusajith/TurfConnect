package com.turfconnect.community.dto;

import com.turfconnect.community.model.MatchStatus;
import com.turfconnect.community.model.MatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private String id;
    private String homeTeamId;
    private String awayTeamId;
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
    private Instant createdAt;
    private Instant updatedAt;
}

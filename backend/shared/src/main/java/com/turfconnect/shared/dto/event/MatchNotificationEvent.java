package com.turfconnect.shared.dto.event;

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
public class MatchNotificationEvent {
    private String matchId;
    private String homeTeamId;
    private String homeTeamName;
    private String awayTeamId;
    private String awayTeamName;
    private String turfId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String matchType;
    private String eventType; // "CHALLENGED", "ACCEPTED", "REJECTED", "CANCELLED", "COMPLETED"
    private Instant occurredAt;
}

package com.turfconnect.tournament.dto;

import com.turfconnect.tournament.model.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {
    private String id;
    private String name;
    private String description;
    private String turfId;
    private String sportType;
    private int maxTeams;
    private int currentTeams;
    private BigDecimal prizePool;
    private TournamentStatus status;
    private LocalDate registrationDeadline;
    private LocalDate startDate;
    private LocalDate endDate;
    private String winnerTeamId;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}

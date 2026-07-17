package com.turfconnect.tournament.dto;

import com.turfconnect.tournament.model.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRegistrationResponse {
    private String id;
    private String tournamentId;
    private String teamId;
    private String registeredBy;
    private RegistrationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

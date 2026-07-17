package com.turfconnect.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRegistrationRequest {

    @NotBlank(message = "Team ID is required")
    private String teamId;
}

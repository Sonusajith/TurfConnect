package com.turfconnect.tournament.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPointsRequest {

    @NotBlank(message = "Team ID is required")
    private String teamId;

    @Min(value = 0, message = "Points must be non-negative")
    private double points;
}

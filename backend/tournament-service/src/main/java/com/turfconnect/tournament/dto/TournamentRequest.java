package com.turfconnect.tournament.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRequest {

    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;

    @NotBlank(message = "Turf ID is required")
    private String turfId;

    @NotBlank(message = "Sport type is required")
    private String sportType;

    @Min(value = 2, message = "Tournament must have at least 2 teams")
    private int maxTeams;

    @NotNull(message = "Prize pool is required")
    @Min(value = 0, message = "Prize pool cannot be negative")
    private BigDecimal prizePool;

    @NotNull(message = "Registration deadline is required")
    @Future(message = "Registration deadline must be in the future")
    private LocalDate registrationDeadline;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

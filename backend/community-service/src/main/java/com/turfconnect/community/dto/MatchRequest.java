package com.turfconnect.community.dto;

import com.turfconnect.community.model.MatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequest {

    @NotBlank(message = "Home team ID is required")
    private String homeTeamId;

    @NotBlank(message = "Away team ID is required")
    private String awayTeamId;

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Match type is required")
    private MatchType matchType;
    
    @NotBlank(message = "Sport type is required")
    private String sportType;
}

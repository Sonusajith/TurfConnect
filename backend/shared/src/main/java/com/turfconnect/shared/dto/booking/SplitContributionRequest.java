package com.turfconnect.shared.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitContributionRequest {

    @Valid
    @NotEmpty(message = "At least one split member is required")
    private List<SplitContributionMember> members;
}

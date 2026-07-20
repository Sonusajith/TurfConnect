package com.turfconnect.shared.dto.booking;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitContributionMember {

    private String id;

    @NotBlank(message = "Member name is required")
    private String name;

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Contribution amount must be positive")
    private BigDecimal amount;

    @Builder.Default
    private SplitContributionStatus status = SplitContributionStatus.PENDING;
}

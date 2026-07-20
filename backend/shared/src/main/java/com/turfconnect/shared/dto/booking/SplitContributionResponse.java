package com.turfconnect.shared.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitContributionResponse {
    private BigDecimal totalAmount;
    private BigDecimal perMemberAmount;
    private BigDecimal collectedAmount;
    private BigDecimal pendingAmount;
    private Integer memberCount;
    private List<SplitContributionMember> members;
    private LocalDateTime updatedAt;
}

package com.turfconnect.shared.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    // Optional reason for refund (for audit trail / customer support)
    private String reason;

    // The user or system initiating the refund — set by the calling service
    private String initiatedBy;
}

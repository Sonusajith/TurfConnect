package com.turfconnect.community.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRequest {
    @NotBlank(message = "Invitee email is required")
    @Email(message = "Invalid email format")
    private String inviteeEmail;

    private String message;
}

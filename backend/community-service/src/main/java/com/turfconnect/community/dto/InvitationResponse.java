package com.turfconnect.community.dto;

import com.turfconnect.community.model.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private String id;
    private String teamId;
    private String inviterId;
    private String inviteeEmail;
    private String inviteeId;
    private String message;
    private InvitationStatus status;
    private Instant invitedAt;
    private Instant respondedAt;
    private Instant expiresAt;
}

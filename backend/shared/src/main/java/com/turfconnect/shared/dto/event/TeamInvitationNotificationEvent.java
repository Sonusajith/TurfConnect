package com.turfconnect.shared.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Shared event DTO used by notification-service to consume community team invitation events.
 * Mirrors the TeamInvitationEvent produced by community-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvitationNotificationEvent {
    private String invitationId;
    private String teamId;
    private String teamName;
    private String inviterId;
    private String inviterName;
    private String inviteeId;
    private String inviteeEmail;
    private String message;
    private Instant expiresAt;
    private Instant occurredAt;
}

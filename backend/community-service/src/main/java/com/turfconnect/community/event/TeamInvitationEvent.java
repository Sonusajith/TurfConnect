package com.turfconnect.community.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * RabbitMQ event published when a team invitation is sent.
 * Consumed by notification-service to send an email to the invitee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvitationEvent {
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

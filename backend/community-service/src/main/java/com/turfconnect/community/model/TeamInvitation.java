package com.turfconnect.community.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "team_invitations")
public class TeamInvitation {
    @Id
    private String id;
    
    private String teamId;
    
    private String inviterId; // userId of the captain/co-captain sending invite
    private String inviteeEmail;
    private String inviteeId; // Resolved internal userId of the invitee
    
    private String message;
    
    private InvitationStatus status;
    
    private String createdBy;
    
    private Instant invitedAt;
    private Instant respondedAt;
    private Instant expiresAt;
    
    private Instant createdAt;
    private Instant updatedAt;
}

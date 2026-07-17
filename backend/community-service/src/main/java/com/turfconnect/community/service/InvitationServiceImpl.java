package com.turfconnect.community.service;

import com.turfconnect.community.client.AuthServiceClient;
import com.turfconnect.community.config.RabbitMQConfig;
import com.turfconnect.community.dto.InvitationRequest;
import com.turfconnect.community.dto.InvitationResponse;
import com.turfconnect.community.event.TeamInvitationEvent;
import com.turfconnect.community.model.*;
import com.turfconnect.community.repository.TeamInvitationRepository;
import com.turfconnect.community.repository.TeamRepository;
import com.turfconnect.shared.dto.user.UserDTO;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {

    private final TeamRepository teamRepository;
    private final TeamInvitationRepository invitationRepository;
    private final AuthServiceClient authServiceClient;
    private final RabbitTemplate rabbitTemplate;

    private static final int INVITATION_EXPIRY_DAYS = 7;

    @Override
    public InvitationResponse sendInvitation(String teamId, InvitationRequest request, String inviterId) {
        // 1. Load team and validate it exists and is active
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new BadRequestException("Cannot invite to an inactive or archived team");
        }

        // 2. Verify inviter is CAPTAIN or CO_CAPTAIN
        TeamMember inviterMember = team.getMembers().stream()
                .filter(m -> m.getUserId().equals(inviterId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("You are not a member of this team"));

        if (inviterMember.getRole() != TeamRole.CAPTAIN && inviterMember.getRole() != TeamRole.CO_CAPTAIN) {
            throw new ForbiddenException("Only CAPTAIN or CO_CAPTAIN can send invitations");
        }

        // 3. Resolve the invitee email to a userId via auth-service
        UserDTO invitee = authServiceClient.getUserByEmail(request.getInviteeEmail());
        if (invitee == null) {
            throw new BadRequestException("No user found with email: " + request.getInviteeEmail());
        }

        // 4. Block inviting existing members
        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(invitee.getId()));
        if (alreadyMember) {
            throw new BadRequestException("User is already a member of this team");
        }

        // 5. Block duplicate pending invitations
        if (invitationRepository.existsByTeamIdAndInviteeIdAndStatus(teamId, invitee.getId(), InvitationStatus.PENDING)) {
            throw new BadRequestException("A pending invitation already exists for this user");
        }

        // 6. Enforce max team size
        if (team.getMaxMembers() != null && team.getMembers().size() >= team.getMaxMembers()) {
            throw new BadRequestException("Team has reached the maximum number of members (" + team.getMaxMembers() + ")");
        }

        // 7. Persist the invitation
        Instant now = Instant.now();
        TeamInvitation invitation = TeamInvitation.builder()
                .teamId(teamId)
                .inviterId(inviterId)
                .inviteeEmail(request.getInviteeEmail())
                .inviteeId(invitee.getId())
                .message(request.getMessage())
                .status(InvitationStatus.PENDING)
                .createdBy(inviterId)
                .invitedAt(now)
                .expiresAt(now.plus(INVITATION_EXPIRY_DAYS, ChronoUnit.DAYS))
                .createdAt(now)
                .updatedAt(now)
                .build();

        invitation = invitationRepository.save(invitation);
        log.info("Invitation {} created for team {} to user {}", invitation.getId(), teamId, invitee.getId());

        // 8. Publish event to RabbitMQ for notification-service
        // Get inviter name from the team members list (or default to inviterId)
        String inviterName = inviterId; // Simplified; a real lookup would use auth-service
        publishInvitationEvent(invitation, team, inviterName);

        return mapToResponse(invitation);
    }

    @Override
    public InvitationResponse acceptInvitation(String invitationId, String userId) {
        TeamInvitation invitation = loadAndValidateInvitation(invitationId, userId);

        // Add the user to the team as a PLAYER
        Team team = teamRepository.findById(invitation.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        // Re-check max size at accept time to handle race conditions
        if (team.getMaxMembers() != null && team.getMembers().size() >= team.getMaxMembers()) {
            throw new BadRequestException("Team is now full; invitation cannot be accepted");
        }

        // Block if already a member (defensive check)
        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId));
        if (alreadyMember) {
            throw new BadRequestException("You are already a member of this team");
        }

        team.getMembers().add(TeamMember.builder()
                .userId(userId)
                .role(TeamRole.PLAYER)
                .joinedAt(Instant.now())
                .build());
        team.setUpdatedAt(Instant.now());
        teamRepository.save(team);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(Instant.now());
        invitation.setUpdatedAt(Instant.now());
        invitationRepository.save(invitation);
        log.info("Invitation {} accepted by user {}", invitationId, userId);

        return mapToResponse(invitation);
    }

    @Override
    public InvitationResponse declineInvitation(String invitationId, String userId) {
        TeamInvitation invitation = loadAndValidateInvitation(invitationId, userId);

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(Instant.now());
        invitation.setUpdatedAt(Instant.now());
        invitationRepository.save(invitation);
        log.info("Invitation {} declined by user {}", invitationId, userId);

        return mapToResponse(invitation);
    }

    @Override
    public List<InvitationResponse> getPendingInvitations(String userId) {
        return invitationRepository.findByInviteeIdAndStatus(userId, InvitationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Private helpers ---

    /**
     * Load an invitation and validate ownership + expiry before any state transition.
     */
    private TeamInvitation loadAndValidateInvitation(String invitationId, String userId) {
        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInviteeId().equals(userId)) {
            throw new ForbiddenException("This invitation does not belong to you");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation is no longer pending (status: " + invitation.getStatus() + ")");
        }

        // Block expired invitations from being accepted
        if (invitation.getExpiresAt() != null && Instant.now().isAfter(invitation.getExpiresAt())) {
            // Mark it as EXPIRED for consistency
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        return invitation;
    }

    /**
     * Publish a team invitation event to RabbitMQ.
     * Uses routing key "community.invitation.sent" on the community exchange.
     */
    private void publishInvitationEvent(TeamInvitation invitation, Team team, String inviterName) {
        try {
            TeamInvitationEvent event = TeamInvitationEvent.builder()
                    .invitationId(invitation.getId())
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .inviterId(invitation.getInviterId())
                    .inviterName(inviterName)
                    .inviteeId(invitation.getInviteeId())
                    .inviteeEmail(invitation.getInviteeEmail())
                    .message(invitation.getMessage())
                    .expiresAt(invitation.getExpiresAt())
                    .occurredAt(Instant.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COMMUNITY_EXCHANGE,
                    "community.invitation.sent",
                    event
            );
            log.info("Published TeamInvitationEvent for invitation: {}", invitation.getId());
        } catch (Exception e) {
            // Fail-safe: log and continue — the invitation is already saved to DB
            log.error("Failed to publish TeamInvitationEvent for invitation: {}. Error: {}",
                    invitation.getId(), e.getMessage());
        }
    }

    private InvitationResponse mapToResponse(TeamInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .teamId(invitation.getTeamId())
                .inviterId(invitation.getInviterId())
                .inviteeEmail(invitation.getInviteeEmail())
                .inviteeId(invitation.getInviteeId())
                .message(invitation.getMessage())
                .status(invitation.getStatus())
                .invitedAt(invitation.getInvitedAt())
                .respondedAt(invitation.getRespondedAt())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}

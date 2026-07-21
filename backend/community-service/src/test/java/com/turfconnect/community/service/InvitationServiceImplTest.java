package com.turfconnect.community.service;

import com.turfconnect.community.client.AuthServiceClient;
import com.turfconnect.community.dto.InvitationRequest;
import com.turfconnect.community.dto.InvitationResponse;
import com.turfconnect.community.model.*;
import com.turfconnect.community.repository.TeamInvitationRepository;
import com.turfconnect.community.repository.TeamRepository;
import com.turfconnect.shared.dto.user.UserDTO;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationServiceImpl Tests")
class InvitationServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private TeamInvitationRepository invitationRepository;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InvitationServiceImpl invitationService;

    private static final String CAPTAIN_ID = "captain-001";
    private static final String INVITEE_ID = "invitee-002";
    private static final String TEAM_ID = "team-abc";
    private static final String INVITATION_ID = "inv-xyz";
    private static final String INVITEE_EMAIL = "invitee@example.com";

    private Team activeTeam;
    private InvitationRequest validInviteRequest;
    private UserDTO inviteeUser;
    private TeamInvitation pendingInvitation;

    @BeforeEach
    void setUp() {
        TeamMember captain = TeamMember.builder()
                .userId(CAPTAIN_ID)
                .role(TeamRole.CAPTAIN)
                .joinedAt(Instant.now())
                .build();

        activeTeam = Team.builder()
                .id(TEAM_ID)
                .name("Thunderbolts FC")
                .status(TeamStatus.ACTIVE)
                .maxMembers(11)
                .createdBy(CAPTAIN_ID)
                .members(new ArrayList<>(List.of(captain)))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        validInviteRequest = InvitationRequest.builder()
                .inviteeEmail(INVITEE_EMAIL)
                .message("Join us!")
                .build();

        inviteeUser = UserDTO.builder()
                .id(INVITEE_ID)
                .email(INVITEE_EMAIL)
                .name("Test Invitee")
                .build();

        pendingInvitation = TeamInvitation.builder()
                .id(INVITATION_ID)
                .teamId(TEAM_ID)
                .inviterId(CAPTAIN_ID)
                .inviteeEmail(INVITEE_EMAIL)
                .inviteeId(INVITEE_ID)
                .message("Join us!")
                .status(InvitationStatus.PENDING)
                .invitedAt(Instant.now())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // --- sendInvitation ---

    @Test
    @DisplayName("sendInvitation: should create invitation when all rules pass")
    void sendInvitation_success() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(authServiceClient.getUserByEmail(INVITEE_EMAIL)).thenReturn(inviteeUser);
        when(invitationRepository.existsByTeamIdAndInviteeEmailAndStatus(TEAM_ID, INVITEE_EMAIL, InvitationStatus.PENDING)).thenReturn(false);
        when(invitationRepository.save(any(TeamInvitation.class))).thenReturn(pendingInvitation);

        InvitationResponse response = invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(response.getInviteeEmail()).isEqualTo(INVITEE_EMAIL);
        assertThat(response.getTeamId()).isEqualTo(TEAM_ID);
        verify(invitationRepository).save(any(TeamInvitation.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("sendInvitation: should throw BadRequestException when team is not active")
    void sendInvitation_inactiveTeam_throwsBadRequest() {
        activeTeam.setStatus(TeamStatus.ARCHIVED);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive or archived");
    }

    @Test
    @DisplayName("sendInvitation: should throw ForbiddenException when inviter is PLAYER, not CAPTAIN")
    void sendInvitation_playerCannotInvite() {
        String playerId = "player-003";
        activeTeam.getMembers().add(TeamMember.builder()
                .userId(playerId)
                .role(TeamRole.PLAYER)
                .joinedAt(Instant.now())
                .build());
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, playerId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only CAPTAIN or CO_CAPTAIN");
    }

    @Test
    @DisplayName("sendInvitation: should throw ForbiddenException when inviter is not a member")
    void sendInvitation_nonMemberCannotInvite() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, "stranger-999"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    @DisplayName("sendInvitation: should throw BadRequestException when invitee is already a member")
    void sendInvitation_alreadyMember_throwsBadRequest() {
        // Add the invitee as an existing member
        activeTeam.getMembers().add(TeamMember.builder()
                .userId(INVITEE_ID)
                .role(TeamRole.PLAYER)
                .joinedAt(Instant.now())
                .build());
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(authServiceClient.getUserByEmail(INVITEE_EMAIL)).thenReturn(inviteeUser);

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    @DisplayName("sendInvitation: should throw BadRequestException when duplicate pending invite exists")
    void sendInvitation_duplicatePendingInvite() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(authServiceClient.getUserByEmail(INVITEE_EMAIL)).thenReturn(inviteeUser);
        when(invitationRepository.existsByTeamIdAndInviteeEmailAndStatus(TEAM_ID, INVITEE_EMAIL, InvitationStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("pending invitation already exists");
    }

    @Test
    @DisplayName("sendInvitation: should throw BadRequestException when team is at max capacity")
    void sendInvitation_maxCapacityReached() {
        // Fill the team to max capacity (11 members)
        activeTeam.setMaxMembers(1); // max = 1 but captain already counts as 1
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(authServiceClient.getUserByEmail(INVITEE_EMAIL)).thenReturn(inviteeUser);
        when(invitationRepository.existsByTeamIdAndInviteeEmailAndStatus(anyString(), anyString(), any())).thenReturn(false);

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maximum number of members");
    }

    @Test
    @DisplayName("sendInvitation: should throw BadRequestException when invitee email not found")
    void sendInvitation_userNotFound() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(authServiceClient.getUserByEmail(INVITEE_EMAIL)).thenReturn(null);

        assertThatThrownBy(() -> invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No user found with email");
    }

    // --- acceptInvitation ---

    @Test
    @DisplayName("acceptInvitation: should add user as PLAYER and update team")
    void acceptInvitation_success() {
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(pendingInvitation));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(invitationRepository.save(any())).thenReturn(pendingInvitation);
        when(teamRepository.save(any())).thenReturn(activeTeam);

        InvitationResponse response = invitationService.acceptInvitation(INVITATION_ID, INVITEE_ID, INVITEE_EMAIL);

        assertThat(response).isNotNull();
        verify(teamRepository).save(argThat(t -> t.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(INVITEE_ID) && m.getRole() == TeamRole.PLAYER)));
        verify(invitationRepository).save(argThat(i -> i.getStatus() == InvitationStatus.ACCEPTED));
    }

    @Test
    @DisplayName("acceptInvitation: should throw ForbiddenException when wrong user tries to accept")
    void acceptInvitation_wrongUser() {
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(pendingInvitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(INVITATION_ID, "wrong-user", "wrong@example.com"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("does not belong to you");
    }

    @Test
    @DisplayName("acceptInvitation: should throw BadRequestException when invitation is expired")
    void acceptInvitation_expired() {
        pendingInvitation.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(pendingInvitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(INVITATION_ID, INVITEE_ID, INVITEE_EMAIL))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invitation has expired");

        // Should mark it as EXPIRED
        verify(invitationRepository).save(argThat(i -> i.getStatus() == InvitationStatus.EXPIRED));
    }

    @Test
    @DisplayName("acceptInvitation: should throw BadRequestException when invitation is not PENDING")
    void acceptInvitation_alreadyAccepted() {
        pendingInvitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(pendingInvitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(INVITATION_ID, INVITEE_ID, INVITEE_EMAIL))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer pending");
    }

    // --- declineInvitation ---

    @Test
    @DisplayName("declineInvitation: should mark invitation as DECLINED")
    void declineInvitation_success() {
        TeamInvitation declinedInvitation = TeamInvitation.builder()
                .id(INVITATION_ID)
                .teamId(TEAM_ID)
                .inviteeId(INVITEE_ID)
                .inviteeEmail(INVITEE_EMAIL)
                .status(InvitationStatus.DECLINED)
                .invitedAt(Instant.now())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(pendingInvitation));
        when(invitationRepository.save(any())).thenReturn(declinedInvitation);

        InvitationResponse response = invitationService.declineInvitation(INVITATION_ID, INVITEE_ID, INVITEE_EMAIL);

        assertThat(response).isNotNull();
        verify(invitationRepository).save(argThat(i -> i.getStatus() == InvitationStatus.DECLINED));
        verify(teamRepository, never()).save(any()); // team must NOT be modified
    }

    // --- getPendingInvitations ---

    @Test
    @DisplayName("getPendingInvitations: should return list of pending invitations for user")
    void getPendingInvitations_success() {
        when(invitationRepository.findByInviteeIdAndStatus(INVITEE_ID, InvitationStatus.PENDING))
                .thenReturn(List.of(pendingInvitation));

        List<InvitationResponse> responses = invitationService.getPendingInvitations(INVITEE_ID, INVITEE_EMAIL);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("getPendingInvitations: should return empty list when no pending invitations")
    void getPendingInvitations_empty() {
        when(invitationRepository.findByInviteeIdAndStatus(INVITEE_ID, InvitationStatus.PENDING))
                .thenReturn(List.of());

        List<InvitationResponse> responses = invitationService.getPendingInvitations(INVITEE_ID, INVITEE_EMAIL);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("sendInvitation: RabbitMQ failure should not prevent invitation from being created")
    void sendInvitation_rabbitMQFailure_doesNotRollback() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(activeTeam));
        when(authServiceClient.getUserByEmail(INVITEE_EMAIL)).thenReturn(inviteeUser);
        when(invitationRepository.existsByTeamIdAndInviteeEmailAndStatus(TEAM_ID, INVITEE_EMAIL, InvitationStatus.PENDING)).thenReturn(false);
        when(invitationRepository.save(any(TeamInvitation.class))).thenReturn(pendingInvitation);
        // Simulate RabbitMQ failure
        doThrow(new RuntimeException("RabbitMQ connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Should NOT throw — fail-safe design means invitation is still saved
        InvitationResponse response = invitationService.sendInvitation(TEAM_ID, validInviteRequest, CAPTAIN_ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(InvitationStatus.PENDING);
        verify(invitationRepository).save(any(TeamInvitation.class));
    }
}

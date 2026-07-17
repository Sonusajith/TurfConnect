package com.turfconnect.community.service;

import com.turfconnect.community.dto.TeamRequest;
import com.turfconnect.community.dto.TeamResponse;
import com.turfconnect.community.model.*;
import com.turfconnect.community.repository.TeamRepository;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamServiceImpl Tests")
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private TeamRequest validRequest;
    private Team savedTeam;
    private static final String USER_ID = "user-123";
    private static final String TEAM_ID = "team-456";

    @BeforeEach
    void setUp() {
        validRequest = TeamRequest.builder()
                .name("Thunderbolts FC")
                .description("A competitive football team")
                .sportType("FOOTBALL")
                .visibility("PUBLIC")
                .maxMembers(11)
                .build();

        savedTeam = Team.builder()
                .id(TEAM_ID)
                .name("Thunderbolts FC")
                .description("A competitive football team")
                .sportType("FOOTBALL")
                .visibility("PUBLIC")
                .maxMembers(11)
                .status(TeamStatus.ACTIVE)
                .createdBy(USER_ID)
                .members(new ArrayList<>(List.of(
                        TeamMember.builder()
                                .userId(USER_ID)
                                .role(TeamRole.CAPTAIN)
                                .joinedAt(Instant.now())
                                .build()
                )))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // --- createTeam ---

    @Test
    @DisplayName("createTeam: should create team and assign creator as CAPTAIN")
    void createTeam_success() {
        when(teamRepository.existsByName("Thunderbolts FC")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        TeamResponse response = teamService.createTeam(validRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Thunderbolts FC");
        assertThat(response.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(response.getStatus()).isEqualTo(TeamStatus.ACTIVE);
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getRole()).isEqualTo(TeamRole.CAPTAIN);
        assertThat(response.getMembers().get(0).getUserId()).isEqualTo(USER_ID);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @DisplayName("createTeam: should throw BadRequestException when team name already exists")
    void createTeam_duplicateName_throwsBadRequest() {
        when(teamRepository.existsByName("Thunderbolts FC")).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeam(validRequest, USER_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Team name already exists");

        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTeam: should use defaults when optional fields not provided")
    void createTeam_defaultsApplied() {
        TeamRequest minimalRequest = TeamRequest.builder().name("Minimal Team").build();
        Team savedMinimal = Team.builder()
                .id("team-001")
                .name("Minimal Team")
                .sportType("FOOTBALL")
                .visibility("PUBLIC")
                .maxMembers(15)
                .status(TeamStatus.ACTIVE)
                .createdBy(USER_ID)
                .members(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(teamRepository.existsByName("Minimal Team")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(savedMinimal);

        TeamResponse response = teamService.createTeam(minimalRequest, USER_ID);

        assertThat(response.getSportType()).isEqualTo("FOOTBALL");
        assertThat(response.getVisibility()).isEqualTo("PUBLIC");
        assertThat(response.getMaxMembers()).isEqualTo(15);
    }

    // --- getTeamById ---

    @Test
    @DisplayName("getTeamById: should return team for valid ID")
    void getTeamById_success() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(savedTeam));

        TeamResponse response = teamService.getTeamById(TEAM_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEAM_ID);
        assertThat(response.getName()).isEqualTo("Thunderbolts FC");
    }

    @Test
    @DisplayName("getTeamById: should throw ResourceNotFoundException for unknown ID")
    void getTeamById_notFound() {
        when(teamRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team not found");
    }

    // --- getAllTeams ---

    @Test
    @DisplayName("getAllTeams: should return all teams")
    void getAllTeams_success() {
        when(teamRepository.findAll()).thenReturn(List.of(savedTeam));

        List<TeamResponse> teams = teamService.getAllTeams();

        assertThat(teams).hasSize(1);
        assertThat(teams.get(0).getName()).isEqualTo("Thunderbolts FC");
    }

    @Test
    @DisplayName("getAllTeams: should return empty list when no teams exist")
    void getAllTeams_empty() {
        when(teamRepository.findAll()).thenReturn(List.of());

        List<TeamResponse> teams = teamService.getAllTeams();

        assertThat(teams).isEmpty();
    }
}

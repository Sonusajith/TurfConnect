package com.turfconnect.tournament.service;

import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.tournament.client.CommunityServiceClient;
import com.turfconnect.tournament.dto.TournamentRegistrationRequest;
import com.turfconnect.tournament.dto.TournamentRegistrationResponse;
import com.turfconnect.tournament.model.RegistrationStatus;
import com.turfconnect.tournament.model.Tournament;
import com.turfconnect.tournament.model.TournamentRegistration;
import com.turfconnect.tournament.model.TournamentStatus;
import com.turfconnect.tournament.repository.TournamentRegistrationRepository;
import com.turfconnect.tournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentRegistrationServiceImpl Tests")
class TournamentRegistrationServiceImplTest {

    @Mock private TournamentRegistrationRepository registrationRepository;
    @Mock private TournamentRepository tournamentRepository;
    @Mock private CommunityServiceClient communityServiceClient;
    @Mock private LeaderboardService leaderboardService;

    @InjectMocks private TournamentRegistrationServiceImpl registrationService;

    private Tournament openTournament;
    private TournamentRegistration pendingRegistration;
    private final String TOURNAMENT_ID = "t-1";
    private final String TEAM_ID = "team-1";
    private final String USER_ID = "user-1";
    private final String CREATOR_ID = "creator-1";

    @BeforeEach
    void setUp() {
        openTournament = Tournament.builder()
                .id(TOURNAMENT_ID)
                .status(TournamentStatus.OPEN_FOR_REGISTRATION)
                .maxTeams(10)
                .currentTeams(0)
                .createdBy(CREATOR_ID)
                .build();

        pendingRegistration = TournamentRegistration.builder()
                .id("reg-1")
                .tournamentId(TOURNAMENT_ID)
                .teamId(TEAM_ID)
                .status(RegistrationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("registerTeam: success")
    void registerTeam_success() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(openTournament));
        when(registrationRepository.existsByTournamentIdAndTeamId(TOURNAMENT_ID, TEAM_ID)).thenReturn(false);
        when(communityServiceClient.doesTeamExist(eq(TEAM_ID), any())).thenReturn(true);
        when(registrationRepository.save(any(TournamentRegistration.class))).thenAnswer(i -> i.getArgument(0));

        TournamentRegistrationRequest request = new TournamentRegistrationRequest(TEAM_ID);
        TournamentRegistrationResponse response = registrationService.registerTeam(TOURNAMENT_ID, request, USER_ID, "token");

        assertThat(response.getStatus()).isEqualTo(RegistrationStatus.PENDING);
        verify(registrationRepository).save(any(TournamentRegistration.class));
    }

    @Test
    @DisplayName("registerTeam: fails if tournament full")
    void registerTeam_full() {
        openTournament.setCurrentTeams(10);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(openTournament));

        TournamentRegistrationRequest request = new TournamentRegistrationRequest(TEAM_ID);
        assertThatThrownBy(() -> registrationService.registerTeam(TOURNAMENT_ID, request, USER_ID, "token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tournament is full.");
    }

    @Test
    @DisplayName("registerTeam: fails if duplicate registration")
    void registerTeam_duplicate() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(openTournament));
        when(registrationRepository.existsByTournamentIdAndTeamId(TOURNAMENT_ID, TEAM_ID)).thenReturn(true);

        TournamentRegistrationRequest request = new TournamentRegistrationRequest(TEAM_ID);
        assertThatThrownBy(() -> registrationService.registerTeam(TOURNAMENT_ID, request, USER_ID, "token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered or pending");
    }

    @Test
    @DisplayName("approveRegistration: success and adds to leaderboard")
    void approveRegistration_success() {
        when(registrationRepository.findById("reg-1")).thenReturn(Optional.of(pendingRegistration));
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(openTournament));
        when(registrationRepository.save(any(TournamentRegistration.class))).thenAnswer(i -> i.getArgument(0));

        TournamentRegistrationResponse response = registrationService.approveRegistration("reg-1", CREATOR_ID);

        assertThat(response.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(openTournament.getCurrentTeams()).isEqualTo(1);
        verify(leaderboardService).addPoints(TOURNAMENT_ID, TEAM_ID, 0);
    }
}

package com.turfconnect.tournament.service;

import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.tournament.dto.TournamentRequest;
import com.turfconnect.tournament.dto.TournamentResponse;
import com.turfconnect.tournament.model.Tournament;
import com.turfconnect.tournament.model.TournamentStatus;
import com.turfconnect.tournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentServiceImpl Tests")
class TournamentServiceImplTest {

    @Mock private TournamentRepository tournamentRepository;
    @InjectMocks private TournamentServiceImpl tournamentService;

    private Tournament draftTournament;
    private final String USER_ID = "admin-1";
    private final String TOURNAMENT_ID = "t-1";

    @BeforeEach
    void setUp() {
        draftTournament = Tournament.builder()
                .id(TOURNAMENT_ID)
                .name("Summer Cup")
                .turfId("turf-1")
                .status(TournamentStatus.DRAFT)
                .createdBy(USER_ID)
                .build();
    }

    @Test
    @DisplayName("openRegistration: success from DRAFT state")
    void openRegistration_success() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(draftTournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(i -> i.getArgument(0));

        TournamentResponse response = tournamentService.openRegistration(TOURNAMENT_ID, USER_ID);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.OPEN_FOR_REGISTRATION);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    @DisplayName("openRegistration: fails if not creator")
    void openRegistration_unauthorized() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(draftTournament));

        assertThatThrownBy(() -> tournamentService.openRegistration(TOURNAMENT_ID, "other-user"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("startTournament: fails if not OPEN_FOR_REGISTRATION")
    void startTournament_wrongState() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(draftTournament));

        assertThatThrownBy(() -> tournamentService.startTournament(TOURNAMENT_ID, USER_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only OPEN_FOR_REGISTRATION tournaments can be started.");
    }
}

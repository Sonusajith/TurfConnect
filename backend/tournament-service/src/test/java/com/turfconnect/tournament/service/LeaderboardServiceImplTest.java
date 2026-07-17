package com.turfconnect.tournament.service;

import com.turfconnect.shared.exception.ResourceNotFoundException;
import com.turfconnect.tournament.dto.LeaderboardEntryResponse;
import com.turfconnect.tournament.model.RegistrationStatus;
import com.turfconnect.tournament.model.TournamentRegistration;
import com.turfconnect.tournament.repository.TournamentRegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardServiceImpl Tests")
class LeaderboardServiceImplTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private TournamentRegistrationRepository registrationRepository;
    @Mock private ZSetOperations<String, String> zSetOperations;

    @InjectMocks private LeaderboardServiceImpl leaderboardService;

    private final String TOURNAMENT_ID = "t-1";
    private final String TEAM_ID = "team-1";
    private final String REDIS_KEY = "leaderboard:tournament:t-1";

    @BeforeEach
    void setUp() {
        // Need lenient to avoid strict stubbing exceptions since not all tests use zSetOperations
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("addPoints: success updates DB and Redis")
    void addPoints_success() {
        TournamentRegistration reg = TournamentRegistration.builder()
                .tournamentId(TOURNAMENT_ID)
                .teamId(TEAM_ID)
                .points(10)
                .build();
        when(registrationRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(List.of(reg));

        leaderboardService.addPoints(TOURNAMENT_ID, TEAM_ID, 5.0);

        assertThat(reg.getPoints()).isEqualTo(15);
        verify(registrationRepository).save(reg);
        verify(zSetOperations).incrementScore(REDIS_KEY, TEAM_ID, 5.0);
    }

    @Test
    @DisplayName("addPoints: fails if registration not found")
    void addPoints_notFound() {
        when(registrationRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> leaderboardService.addPoints(TOURNAMENT_ID, TEAM_ID, 5.0))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.turfconnect.tournament.service;

import com.turfconnect.shared.exception.ResourceNotFoundException;
import com.turfconnect.tournament.dto.LeaderboardEntryResponse;
import com.turfconnect.tournament.model.RegistrationStatus;
import com.turfconnect.tournament.model.TournamentRegistration;
import com.turfconnect.tournament.repository.TournamentRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

    private final StringRedisTemplate redisTemplate;
    private final TournamentRegistrationRepository registrationRepository;

    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:tournament:";

    @Override
    @Transactional
    public void addPoints(String tournamentId, String teamId, double points) {
        // Persist points to DB for rebuilding capability
        TournamentRegistration registration = registrationRepository.findByTournamentId(tournamentId)
                .stream()
                .filter(r -> r.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found for team " + teamId + " in tournament " + tournamentId));

        registration.setPoints(registration.getPoints() + (int) points);
        registrationRepository.save(registration);

        // Update Redis
        String key = LEADERBOARD_KEY_PREFIX + tournamentId;
        redisTemplate.opsForZSet().incrementScore(key, teamId, points);
        log.info("Added {} points to team {} in tournament {}", points, teamId, tournamentId);
    }

    @Override
    public List<LeaderboardEntryResponse> getLeaderboard(String tournamentId) {
        String key = LEADERBOARD_KEY_PREFIX + tournamentId;

        // Try to fetch from Redis
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            log.warn("Leaderboard not found in Redis for tournament {}. Rebuilding from DB.", tournamentId);
            tuples = rebuildLeaderboardFromDb(tournamentId, key);
        }

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        AtomicInteger rank = new AtomicInteger(1);

        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                leaderboard.add(LeaderboardEntryResponse.builder()
                        .rank(rank.getAndIncrement())
                        .teamId(tuple.getValue())
                        .points(tuple.getScore() != null ? tuple.getScore() : 0.0)
                        .build());
            }
        }
        return leaderboard;
    }

    private Set<ZSetOperations.TypedTuple<String>> rebuildLeaderboardFromDb(String tournamentId, String key) {
        List<TournamentRegistration> registrations = registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.APPROVED);
        if (registrations.isEmpty()) {
            return null;
        }

        for (TournamentRegistration reg : registrations) {
            redisTemplate.opsForZSet().add(key, reg.getTeamId(), reg.getPoints());
        }
        
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
    }
}

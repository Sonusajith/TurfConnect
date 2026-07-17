package com.turfconnect.tournament.service;

import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import com.turfconnect.tournament.dto.TournamentRequest;
import com.turfconnect.tournament.dto.TournamentResponse;
import com.turfconnect.tournament.model.Tournament;
import com.turfconnect.tournament.model.TournamentStatus;
import com.turfconnect.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;

    @Override
    @Transactional
    public TournamentResponse createTournament(TournamentRequest request, String userId) {
        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .description(request.getDescription())
                .turfId(request.getTurfId())
                .sportType(request.getSportType())
                .maxTeams(request.getMaxTeams())
                .currentTeams(0)
                .prizePool(request.getPrizePool())
                .status(TournamentStatus.DRAFT)
                .registrationDeadline(request.getRegistrationDeadline())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Tournament saved = tournamentRepository.save(tournament);
        log.info("Tournament {} created by user {}", saved.getId(), userId);
        return mapToResponse(saved);
    }

    @Override
    public TournamentResponse getTournament(String tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        return mapToResponse(tournament);
    }

    @Override
    public List<TournamentResponse> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TournamentResponse openRegistration(String tournamentId, String userId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        verifyOwner(tournament, userId);

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT tournaments can be opened for registration.");
        }

        tournament.setStatus(TournamentStatus.OPEN_FOR_REGISTRATION);
        tournament.setUpdatedAt(Instant.now());
        Tournament saved = tournamentRepository.save(tournament);
        
        log.info("Tournament {} opened for registration by user {}", tournamentId, userId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse startTournament(String tournamentId, String userId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        verifyOwner(tournament, userId);

        if (tournament.getStatus() != TournamentStatus.OPEN_FOR_REGISTRATION) {
            throw new BadRequestException("Only OPEN_FOR_REGISTRATION tournaments can be started.");
        }

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament.setUpdatedAt(Instant.now());
        Tournament saved = tournamentRepository.save(tournament);

        log.info("Tournament {} started by user {}", tournamentId, userId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse completeTournament(String tournamentId, String winnerTeamId, String userId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        verifyOwner(tournament, userId);

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BadRequestException("Only IN_PROGRESS tournaments can be completed.");
        }

        tournament.setStatus(TournamentStatus.COMPLETED);
        if (winnerTeamId != null) {
            tournament.setWinnerTeamId(winnerTeamId);
        }
        tournament.setUpdatedAt(Instant.now());
        Tournament saved = tournamentRepository.save(tournament);

        log.info("Tournament {} completed by user {} with winner {}", tournamentId, userId, winnerTeamId);
        return mapToResponse(saved);
    }

    private Tournament getTournamentEntity(String tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));
    }

    private void verifyOwner(Tournament tournament, String userId) {
        if (!tournament.getCreatedBy().equals(userId)) {
            // Note: In a real system, we would also check if userId has ORG_ADMIN or TURF_OWNER role via token claims
            throw new ForbiddenException("You are not authorized to modify this tournament.");
        }
    }

    private TournamentResponse mapToResponse(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .turfId(tournament.getTurfId())
                .sportType(tournament.getSportType())
                .maxTeams(tournament.getMaxTeams())
                .currentTeams(tournament.getCurrentTeams())
                .prizePool(tournament.getPrizePool())
                .status(tournament.getStatus())
                .registrationDeadline(tournament.getRegistrationDeadline())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .winnerTeamId(tournament.getWinnerTeamId())
                .createdBy(tournament.getCreatedBy())
                .createdAt(tournament.getCreatedAt())
                .updatedAt(tournament.getUpdatedAt())
                .build();
    }
}

package com.turfconnect.tournament.service;

import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import com.turfconnect.tournament.client.CommunityServiceClient;
import com.turfconnect.tournament.dto.TournamentRegistrationRequest;
import com.turfconnect.tournament.dto.TournamentRegistrationResponse;
import com.turfconnect.tournament.model.RegistrationStatus;
import com.turfconnect.tournament.model.Tournament;
import com.turfconnect.tournament.model.TournamentRegistration;
import com.turfconnect.tournament.model.TournamentStatus;
import com.turfconnect.tournament.repository.TournamentRegistrationRepository;
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
public class TournamentRegistrationServiceImpl implements TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRepository tournamentRepository;
    private final CommunityServiceClient communityServiceClient;
    private final LeaderboardService leaderboardService;

    @Override
    @Transactional
    public TournamentRegistrationResponse registerTeam(String tournamentId, TournamentRegistrationRequest request, String userId, String token) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        if (tournament.getStatus() != TournamentStatus.OPEN_FOR_REGISTRATION) {
            throw new BadRequestException("Tournament is not open for registration.");
        }

        if (tournament.getCurrentTeams() >= tournament.getMaxTeams()) {
            throw new BadRequestException("Tournament is full.");
        }

        if (registrationRepository.existsByTournamentIdAndTeamId(tournamentId, request.getTeamId())) {
            throw new BadRequestException("Team is already registered or pending registration for this tournament.");
        }

        // Call community-service to verify the team exists
        boolean teamExists = communityServiceClient.doesTeamExist(request.getTeamId(), token);
        if (!teamExists) {
            throw new BadRequestException("Invalid team ID or you do not have permission to register this team.");
        }

        TournamentRegistration registration = TournamentRegistration.builder()
                .tournamentId(tournamentId)
                .teamId(request.getTeamId())
                .registeredBy(userId)
                .status(RegistrationStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TournamentRegistration saved = registrationRepository.save(registration);
        log.info("Team {} registered for tournament {} by user {}", request.getTeamId(), tournamentId, userId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TournamentRegistrationResponse approveRegistration(String registrationId, String userId) {
        TournamentRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));

        Tournament tournament = tournamentRepository.findById(registration.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found."));

        if (!tournament.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Only the tournament creator can approve registrations.");
        }

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Only PENDING registrations can be approved.");
        }

        if (tournament.getCurrentTeams() >= tournament.getMaxTeams()) {
            throw new BadRequestException("Tournament is already full.");
        }

        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setUpdatedAt(Instant.now());
        TournamentRegistration saved = registrationRepository.save(registration);

        tournament.setCurrentTeams(tournament.getCurrentTeams() + 1);
        tournamentRepository.save(tournament);

        // Add team to leaderboard with 0 points
        leaderboardService.addPoints(tournament.getId(), registration.getTeamId(), 0);

        log.info("Registration {} approved by user {}", registrationId, userId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TournamentRegistrationResponse rejectRegistration(String registrationId, String userId) {
        TournamentRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));

        Tournament tournament = tournamentRepository.findById(registration.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found."));

        if (!tournament.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Only the tournament creator can reject registrations.");
        }

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Only PENDING registrations can be rejected.");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setUpdatedAt(Instant.now());
        TournamentRegistration saved = registrationRepository.save(registration);

        log.info("Registration {} rejected by user {}", registrationId, userId);
        return mapToResponse(saved);
    }

    @Override
    public List<TournamentRegistrationResponse> getRegistrationsForTournament(String tournamentId) {
        return registrationRepository.findByTournamentId(tournamentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TournamentRegistrationResponse mapToResponse(TournamentRegistration registration) {
        return TournamentRegistrationResponse.builder()
                .id(registration.getId())
                .tournamentId(registration.getTournamentId())
                .teamId(registration.getTeamId())
                .registeredBy(registration.getRegisteredBy())
                .status(registration.getStatus())
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }
}

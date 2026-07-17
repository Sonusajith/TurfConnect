package com.turfconnect.community.service;

import com.turfconnect.community.client.BookingServiceClient;
import com.turfconnect.community.dto.MatchRequest;
import com.turfconnect.community.dto.MatchResponse;
import com.turfconnect.community.model.*;
import com.turfconnect.community.repository.TeamMatchRepository;
import com.turfconnect.community.repository.TeamRepository;
import com.turfconnect.shared.dto.booking.BookingResponse;
import com.turfconnect.shared.dto.booking.BookingStatus;
import com.turfconnect.shared.dto.event.MatchNotificationEvent;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ForbiddenException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final TeamMatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final BookingServiceClient bookingServiceClient;
    private final RabbitTemplate rabbitTemplate;

    private static final String MATCH_EXCHANGE = "community.match.exchange";
    private static final String MATCH_ROUTING_KEY = "community.match.event";

    @Override
    @Transactional
    public MatchResponse createMatchChallenge(MatchRequest request, String userId) {
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new BadRequestException("A team cannot challenge itself.");
        }

        Team homeTeam = getTeam(request.getHomeTeamId());
        Team awayTeam = getTeam(request.getAwayTeamId());

        verifyCaptainOrCoCaptain(homeTeam, userId);

        BookingResponse booking = bookingServiceClient.getBookingById(request.getBookingId());
        if (booking == null) {
            throw new BadRequestException("Invalid booking ID.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking must be CONFIRMED to create a match.");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("You must be the owner of the booking to challenge a team.");
        }

        if (matchRepository.existsByBookingIdAndStatusNot(booking.getId(), MatchStatus.CANCELLED)) {
            throw new BadRequestException("This booking is already associated with an active match challenge.");
        }

        // Double booking check for home team
        checkDoubleBooking(homeTeam.getId(), booking);
        // Double booking check for away team
        checkDoubleBooking(awayTeam.getId(), booking);

        TeamMatch match = TeamMatch.builder()
                .homeTeamId(homeTeam.getId())
                .awayTeamId(awayTeam.getId())
                .bookingId(booking.getId())
                .turfId(booking.getTurfId())
                .date(booking.getDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .matchType(request.getMatchType())
                .sportType(request.getSportType())
                .status(MatchStatus.CHALLENGED)
                .createdBy(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TeamMatch savedMatch = matchRepository.save(match);

        publishMatchEvent(savedMatch, homeTeam.getName(), awayTeam.getName(), "CHALLENGED");

        log.info("Match {} challenged by user {} for home team {}", savedMatch.getId(), userId, homeTeam.getId());
        return mapToResponse(savedMatch);
    }

    @Override
    @Transactional
    public MatchResponse acceptMatch(String matchId, String userId) {
        TeamMatch match = getMatch(matchId);
        Team awayTeam = getTeam(match.getAwayTeamId());
        
        verifyCaptainOrCoCaptain(awayTeam, userId);

        if (match.getStatus() != MatchStatus.CHALLENGED) {
            throw new BadRequestException("Match is not in CHALLENGED state.");
        }

        // Re-verify double booking for away team just in case they accepted another match in the meantime
        BookingResponse booking = bookingServiceClient.getBookingById(match.getBookingId());
        checkDoubleBooking(awayTeam.getId(), booking);

        match.setStatus(MatchStatus.ACCEPTED);
        match.setUpdatedAt(Instant.now());
        TeamMatch updatedMatch = matchRepository.save(match);

        Team homeTeam = getTeam(match.getHomeTeamId());
        publishMatchEvent(updatedMatch, homeTeam.getName(), awayTeam.getName(), "ACCEPTED");

        log.info("Match {} accepted by user {}", matchId, userId);
        return mapToResponse(updatedMatch);
    }

    @Override
    @Transactional
    public MatchResponse rejectMatch(String matchId, String userId) {
        TeamMatch match = getMatch(matchId);
        Team awayTeam = getTeam(match.getAwayTeamId());

        verifyCaptainOrCoCaptain(awayTeam, userId);

        if (match.getStatus() != MatchStatus.CHALLENGED) {
            throw new BadRequestException("Match is not in CHALLENGED state.");
        }

        match.setStatus(MatchStatus.REJECTED);
        match.setUpdatedAt(Instant.now());
        TeamMatch updatedMatch = matchRepository.save(match);

        Team homeTeam = getTeam(match.getHomeTeamId());
        publishMatchEvent(updatedMatch, homeTeam.getName(), awayTeam.getName(), "REJECTED");

        log.info("Match {} rejected by user {}", matchId, userId);
        return mapToResponse(updatedMatch);
    }

    @Override
    @Transactional
    public MatchResponse cancelMatch(String matchId, String userId) {
        TeamMatch match = getMatch(matchId);
        Team homeTeam = getTeam(match.getHomeTeamId());

        verifyCaptainOrCoCaptain(homeTeam, userId);

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a match that is already completed or cancelled.");
        }

        match.setStatus(MatchStatus.CANCELLED);
        match.setUpdatedAt(Instant.now());
        TeamMatch updatedMatch = matchRepository.save(match);

        Team awayTeam = getTeam(match.getAwayTeamId());
        publishMatchEvent(updatedMatch, homeTeam.getName(), awayTeam.getName(), "CANCELLED");

        log.info("Match {} cancelled by user {}", matchId, userId);
        return mapToResponse(updatedMatch);
    }

    @Override
    @Transactional
    public MatchResponse completeMatch(String matchId, String winnerTeamId, String userId) {
        TeamMatch match = getMatch(matchId);
        Team homeTeam = getTeam(match.getHomeTeamId());

        verifyCaptainOrCoCaptain(homeTeam, userId);

        if (match.getStatus() != MatchStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED matches can be COMPLETED.");
        }

        if (winnerTeamId != null && !winnerTeamId.equals(match.getHomeTeamId()) && !winnerTeamId.equals(match.getAwayTeamId())) {
            throw new BadRequestException("Winner must be either home or away team.");
        }

        match.setStatus(MatchStatus.COMPLETED);
        match.setWinnerTeamId(winnerTeamId);
        match.setUpdatedAt(Instant.now());
        TeamMatch updatedMatch = matchRepository.save(match);

        Team awayTeam = getTeam(match.getAwayTeamId());
        publishMatchEvent(updatedMatch, homeTeam.getName(), awayTeam.getName(), "COMPLETED");

        log.info("Match {} completed by user {}", matchId, userId);
        return mapToResponse(updatedMatch);
    }

    @Override
    public List<MatchResponse> getMatchesForTeam(String teamId) {
        return matchRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MatchResponse getMatchById(String matchId) {
        return mapToResponse(getMatch(matchId));
    }

    private Team getTeam(String teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }

    private TeamMatch getMatch(String matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));
    }

    private void verifyCaptainOrCoCaptain(Team team, String userId) {
        boolean isAuthorized = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId) && 
                        (m.getRole() == TeamRole.CAPTAIN || m.getRole() == TeamRole.CO_CAPTAIN));
        if (!isAuthorized) {
            throw new ForbiddenException("Only CAPTAIN or CO_CAPTAIN can perform this action for team: " + team.getName());
        }
    }

    private void checkDoubleBooking(String teamId, BookingResponse booking) {
        List<TeamMatch> overlapping = matchRepository.findOverlappingMatchesForTeam(
                teamId, booking.getDate(), booking.getStartTime(), booking.getEndTime()
        );
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Team " + teamId + " is already booked for a match at this time.");
        }
    }

    private void publishMatchEvent(TeamMatch match, String homeTeamName, String awayTeamName, String eventType) {
        MatchNotificationEvent event = MatchNotificationEvent.builder()
                .matchId(match.getId())
                .homeTeamId(match.getHomeTeamId())
                .homeTeamName(homeTeamName)
                .awayTeamId(match.getAwayTeamId())
                .awayTeamName(awayTeamName)
                .turfId(match.getTurfId())
                .date(match.getDate())
                .startTime(match.getStartTime())
                .endTime(match.getEndTime())
                .matchType(match.getMatchType() != null ? match.getMatchType().name() : null)
                .eventType(eventType)
                .occurredAt(Instant.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(MATCH_EXCHANGE, MATCH_ROUTING_KEY, event);
            log.info("Published MatchNotificationEvent for match: {}", match.getId());
        } catch (Exception e) {
            log.error("Failed to publish MatchNotificationEvent for match: {}. Error: {}", match.getId(), e.getMessage());
            // Intentionally not throwing to allow the transaction to complete despite messaging failure
        }
    }

    private MatchResponse mapToResponse(TeamMatch match) {
        return MatchResponse.builder()
                .id(match.getId())
                .homeTeamId(match.getHomeTeamId())
                .awayTeamId(match.getAwayTeamId())
                .bookingId(match.getBookingId())
                .turfId(match.getTurfId())
                .date(match.getDate())
                .startTime(match.getStartTime())
                .endTime(match.getEndTime())
                .matchType(match.getMatchType())
                .sportType(match.getSportType())
                .status(match.getStatus())
                .winnerTeamId(match.getWinnerTeamId())
                .createdBy(match.getCreatedBy())
                .createdAt(match.getCreatedAt())
                .updatedAt(match.getUpdatedAt())
                .build();
    }
}

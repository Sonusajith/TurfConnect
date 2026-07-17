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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchServiceImpl Tests")
class MatchServiceImplTest {

    @Mock private TeamMatchRepository matchRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private BookingServiceClient bookingServiceClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Team homeTeam;
    private Team awayTeam;
    private BookingResponse booking;
    private MatchRequest validRequest;
    private TeamMatch existingMatch;

    private static final String CAPTAIN_ID = "home-captain";
    private static final String AWAY_CAPTAIN_ID = "away-captain";
    private static final String HOME_TEAM_ID = "team-1";
    private static final String AWAY_TEAM_ID = "team-2";
    private static final String BOOKING_ID = "booking-1";
    private static final String MATCH_ID = "match-1";

    @BeforeEach
    void setUp() {
        homeTeam = Team.builder()
                .id(HOME_TEAM_ID)
                .name("Home Team")
                .members(new ArrayList<>(List.of(
                        TeamMember.builder().userId(CAPTAIN_ID).role(TeamRole.CAPTAIN).build()
                )))
                .build();

        awayTeam = Team.builder()
                .id(AWAY_TEAM_ID)
                .name("Away Team")
                .members(new ArrayList<>(List.of(
                        TeamMember.builder().userId(AWAY_CAPTAIN_ID).role(TeamRole.CAPTAIN).build()
                )))
                .build();

        booking = BookingResponse.builder()
                .id(BOOKING_ID)
                .userId(CAPTAIN_ID)
                .status(BookingStatus.CONFIRMED)
                .date(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .turfId("turf-1")
                .build();

        validRequest = MatchRequest.builder()
                .homeTeamId(HOME_TEAM_ID)
                .awayTeamId(AWAY_TEAM_ID)
                .bookingId(BOOKING_ID)
                .matchType(MatchType.FRIENDLY)
                .sportType("FOOTBALL")
                .build();

        existingMatch = TeamMatch.builder()
                .id(MATCH_ID)
                .homeTeamId(HOME_TEAM_ID)
                .awayTeamId(AWAY_TEAM_ID)
                .bookingId(BOOKING_ID)
                .status(MatchStatus.CHALLENGED)
                .build();
    }

    @Test
    @DisplayName("createMatchChallenge: self challenge throws exception")
    void createMatchChallenge_selfChallenge() {
        validRequest.setAwayTeamId(HOME_TEAM_ID);
        assertThatThrownBy(() -> matchService.createMatchChallenge(validRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot challenge itself");
    }

    @Test
    @DisplayName("createMatchChallenge: not captain throws exception")
    void createMatchChallenge_notCaptain() {
        when(teamRepository.findById(HOME_TEAM_ID)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(AWAY_TEAM_ID)).thenReturn(Optional.of(awayTeam));

        assertThatThrownBy(() -> matchService.createMatchChallenge(validRequest, "some-other-user"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only CAPTAIN or CO_CAPTAIN");
    }

    @Test
    @DisplayName("createMatchChallenge: booking not confirmed throws exception")
    void createMatchChallenge_bookingNotConfirmed() {
        booking.setStatus(BookingStatus.PENDING);
        when(teamRepository.findById(HOME_TEAM_ID)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(AWAY_TEAM_ID)).thenReturn(Optional.of(awayTeam));
        when(bookingServiceClient.getBookingById(BOOKING_ID)).thenReturn(booking);

        assertThatThrownBy(() -> matchService.createMatchChallenge(validRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be CONFIRMED");
    }

    @Test
    @DisplayName("createMatchChallenge: booking owner mismatch throws exception")
    void createMatchChallenge_notBookingOwner() {
        booking.setUserId("someone-else");
        when(teamRepository.findById(HOME_TEAM_ID)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(AWAY_TEAM_ID)).thenReturn(Optional.of(awayTeam));
        when(bookingServiceClient.getBookingById(BOOKING_ID)).thenReturn(booking);

        assertThatThrownBy(() -> matchService.createMatchChallenge(validRequest, CAPTAIN_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("owner of the booking");
    }

    @Test
    @DisplayName("createMatchChallenge: successful creation publishes event")
    void createMatchChallenge_success() {
        when(teamRepository.findById(HOME_TEAM_ID)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(AWAY_TEAM_ID)).thenReturn(Optional.of(awayTeam));
        when(bookingServiceClient.getBookingById(BOOKING_ID)).thenReturn(booking);
        when(matchRepository.existsByBookingIdAndStatusNot(BOOKING_ID, MatchStatus.CANCELLED)).thenReturn(false);
        when(matchRepository.save(any(TeamMatch.class))).thenAnswer(i -> {
            TeamMatch m = i.getArgument(0);
            m.setId("new-match");
            return m;
        });

        MatchResponse response = matchService.createMatchChallenge(validRequest, CAPTAIN_ID);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(MatchStatus.CHALLENGED);
        verify(matchRepository).save(any(TeamMatch.class));
        verify(rabbitTemplate).convertAndSend(eq("community.match.exchange"), eq("community.match.event"), any(MatchNotificationEvent.class));
    }

    @Test
    @DisplayName("createMatchChallenge: double booking throws exception")
    void createMatchChallenge_doubleBooking() {
        when(teamRepository.findById(HOME_TEAM_ID)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(AWAY_TEAM_ID)).thenReturn(Optional.of(awayTeam));
        when(bookingServiceClient.getBookingById(BOOKING_ID)).thenReturn(booking);
        when(matchRepository.existsByBookingIdAndStatusNot(BOOKING_ID, MatchStatus.CANCELLED)).thenReturn(false);

        // Simulate home team already has a match at this time
        when(matchRepository.findOverlappingMatchesForTeam(eq(HOME_TEAM_ID), any(), any(), any()))
                .thenReturn(List.of(new TeamMatch()));

        assertThatThrownBy(() -> matchService.createMatchChallenge(validRequest, CAPTAIN_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already booked for a match");
    }
}

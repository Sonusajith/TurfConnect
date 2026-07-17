package com.turfconnect.community.controller;

import com.turfconnect.community.dto.MatchRequest;
import com.turfconnect.community.dto.MatchResponse;
import com.turfconnect.community.service.MatchService;
import com.turfconnect.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<ApiResponse<MatchResponse>> createMatch(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody MatchRequest request) {

        log.info("User {} challenging team {} to a match against team {} with booking {}", 
                userId, request.getAwayTeamId(), request.getHomeTeamId(), request.getBookingId());
        
        MatchResponse response = matchService.createMatchChallenge(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{matchId}/accept")
    public ResponseEntity<ApiResponse<MatchResponse>> acceptMatch(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId) {

        log.info("User {} accepting match {}", userId, matchId);
        MatchResponse response = matchService.acceptMatch(matchId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{matchId}/reject")
    public ResponseEntity<ApiResponse<MatchResponse>> rejectMatch(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId) {

        log.info("User {} rejecting match {}", userId, matchId);
        MatchResponse response = matchService.rejectMatch(matchId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{matchId}/cancel")
    public ResponseEntity<ApiResponse<MatchResponse>> cancelMatch(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId) {

        log.info("User {} cancelling match {}", userId, matchId);
        MatchResponse response = matchService.cancelMatch(matchId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{matchId}/complete")
    public ResponseEntity<ApiResponse<MatchResponse>> completeMatch(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId,
            @RequestParam(required = false) String winnerTeamId) {

        log.info("User {} completing match {} with winner {}", userId, matchId, winnerTeamId);
        MatchResponse response = matchService.completeMatch(matchId, winnerTeamId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getTeamMatches(@PathVariable String teamId) {
        List<MatchResponse> responses = matchService.getMatchesForTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatchById(@PathVariable String matchId) {
        MatchResponse response = matchService.getMatchById(matchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

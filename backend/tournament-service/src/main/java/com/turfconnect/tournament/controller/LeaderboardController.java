package com.turfconnect.tournament.controller;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.tournament.dto.AddPointsRequest;
import com.turfconnect.tournament.dto.LeaderboardEntryResponse;
import com.turfconnect.tournament.service.LeaderboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboards")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> getLeaderboard(@PathVariable String tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getLeaderboard(tournamentId)));
    }

    @PostMapping("/{tournamentId}/points")
    public ResponseEntity<ApiResponse<String>> addPoints(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String tournamentId,
            @Valid @RequestBody AddPointsRequest request) {
        log.info("User {} adding {} points to team {} in tournament {}", userId, request.getPoints(), request.getTeamId(), tournamentId);
        
        // In a real scenario, this would check if the user is an admin or the match result was verified
        leaderboardService.addPoints(tournamentId, request.getTeamId(), request.getPoints());
        
        return ResponseEntity.ok(ApiResponse.success("Points added successfully"));
    }
}

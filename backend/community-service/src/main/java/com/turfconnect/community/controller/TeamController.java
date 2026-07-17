package com.turfconnect.community.controller;

import com.turfconnect.community.dto.TeamRequest;
import com.turfconnect.community.dto.TeamResponse;
import com.turfconnect.community.service.TeamService;
import com.turfconnect.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;

    /**
     * POST /api/v1/teams
     * Create a new team. The caller becomes the CAPTAIN automatically.
     * Requires X-User-Id header injected by the API gateway from the JWT.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TeamRequest request) {

        log.info("User {} creating team: {}", userId, request.getName());
        TeamResponse response = teamService.createTeam(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/teams/{teamId}
     * Get a team by its ID.
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(@PathVariable String teamId) {
        TeamResponse response = teamService.getTeamById(teamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/teams
     * List all teams.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(ApiResponse.success(teams));
    }
}

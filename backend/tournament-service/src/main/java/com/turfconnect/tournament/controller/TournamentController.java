package com.turfconnect.tournament.controller;

import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.tournament.dto.TournamentRegistrationRequest;
import com.turfconnect.tournament.dto.TournamentRegistrationResponse;
import com.turfconnect.tournament.dto.TournamentRequest;
import com.turfconnect.tournament.dto.TournamentResponse;
import com.turfconnect.tournament.service.TournamentRegistrationService;
import com.turfconnect.tournament.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
@Slf4j
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentRegistrationService registrationService;

    // --- Tournament Management ---

    @PostMapping
    public ResponseEntity<ApiResponse<TournamentResponse>> createTournament(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TournamentRequest request) {
        log.info("Creating tournament by user {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tournamentService.createTournament(request, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getAllTournaments() {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.getAllTournaments()));
    }

    @GetMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<TournamentResponse>> getTournament(@PathVariable String tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.getTournament(tournamentId)));
    }

    @PutMapping("/{tournamentId}/open")
    public ResponseEntity<ApiResponse<TournamentResponse>> openRegistration(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.openRegistration(tournamentId, userId)));
    }

    @PutMapping("/{tournamentId}/start")
    public ResponseEntity<ApiResponse<TournamentResponse>> startTournament(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.startTournament(tournamentId, userId)));
    }

    @PutMapping("/{tournamentId}/complete")
    public ResponseEntity<ApiResponse<TournamentResponse>> completeTournament(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String tournamentId,
            @RequestParam(required = false) String winnerTeamId) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.completeTournament(tournamentId, winnerTeamId, userId)));
    }

    // --- Tournament Registration ---

    @PostMapping("/{tournamentId}/registrations")
    public ResponseEntity<ApiResponse<TournamentRegistrationResponse>> registerTeam(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String tournamentId,
            @Valid @RequestBody TournamentRegistrationRequest request) {
        log.info("Registering team {} for tournament {} by user {}", request.getTeamId(), tournamentId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(registrationService.registerTeam(tournamentId, request, userId, token)));
    }

    @GetMapping("/{tournamentId}/registrations")
    public ResponseEntity<ApiResponse<List<TournamentRegistrationResponse>>> getRegistrations(@PathVariable String tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.getRegistrationsForTournament(tournamentId)));
    }

    @PutMapping("/registrations/{registrationId}/approve")
    public ResponseEntity<ApiResponse<TournamentRegistrationResponse>> approveRegistration(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String registrationId) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.approveRegistration(registrationId, userId)));
    }

    @PutMapping("/registrations/{registrationId}/reject")
    public ResponseEntity<ApiResponse<TournamentRegistrationResponse>> rejectRegistration(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String registrationId) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.rejectRegistration(registrationId, userId)));
    }
}

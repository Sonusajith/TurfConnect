package com.turfconnect.community.controller;

import com.turfconnect.community.dto.InvitationRequest;
import com.turfconnect.community.dto.InvitationResponse;
import com.turfconnect.community.service.InvitationService;
import com.turfconnect.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Slf4j
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * POST /api/v1/invitations/teams/{teamId}
     * Send an invitation to a user by email. Only CAPTAIN or CO_CAPTAIN may call this.
     */
    @PostMapping("/teams/{teamId}")
    public ResponseEntity<ApiResponse<InvitationResponse>> sendInvitation(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String teamId,
            @Valid @RequestBody InvitationRequest request) {

        log.info("User {} sending invite for team {}", userId, teamId);
        InvitationResponse response = invitationService.sendInvitation(teamId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * PUT /api/v1/invitations/{invitationId}/accept
     * Accept a pending invitation.
     */
    @PutMapping("/{invitationId}/accept")
    public ResponseEntity<ApiResponse<InvitationResponse>> acceptInvitation(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @PathVariable String invitationId) {

        log.info("User {} accepting invitation {}", userId, invitationId);
        InvitationResponse response = invitationService.acceptInvitation(invitationId, userId, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PUT /api/v1/invitations/{invitationId}/decline
     * Decline a pending invitation.
     */
    @PutMapping("/{invitationId}/decline")
    public ResponseEntity<ApiResponse<InvitationResponse>> declineInvitation(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @PathVariable String invitationId) {

        log.info("User {} declining invitation {}", userId, invitationId);
        InvitationResponse response = invitationService.declineInvitation(invitationId, userId, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/invitations/me
     * Get all pending invitations for the calling user.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyPendingInvitations(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email) {

        List<InvitationResponse> invitations = invitationService.getPendingInvitations(userId, email);
        return ResponseEntity.ok(ApiResponse.success(invitations));
    }
}

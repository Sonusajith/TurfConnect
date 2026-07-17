package com.turfconnect.community.service;

import com.turfconnect.community.dto.InvitationRequest;
import com.turfconnect.community.dto.InvitationResponse;

import java.util.List;

public interface InvitationService {
    InvitationResponse sendInvitation(String teamId, InvitationRequest request, String inviterId);
    InvitationResponse acceptInvitation(String invitationId, String userId);
    InvitationResponse declineInvitation(String invitationId, String userId);
    List<InvitationResponse> getPendingInvitations(String userId);
}

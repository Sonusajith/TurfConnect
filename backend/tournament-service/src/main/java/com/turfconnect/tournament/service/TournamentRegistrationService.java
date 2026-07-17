package com.turfconnect.tournament.service;

import com.turfconnect.tournament.dto.TournamentRegistrationRequest;
import com.turfconnect.tournament.dto.TournamentRegistrationResponse;

import java.util.List;

public interface TournamentRegistrationService {
    TournamentRegistrationResponse registerTeam(String tournamentId, TournamentRegistrationRequest request, String userId, String token);
    TournamentRegistrationResponse approveRegistration(String registrationId, String userId);
    TournamentRegistrationResponse rejectRegistration(String registrationId, String userId);
    List<TournamentRegistrationResponse> getRegistrationsForTournament(String tournamentId);
}

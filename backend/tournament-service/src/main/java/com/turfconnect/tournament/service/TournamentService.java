package com.turfconnect.tournament.service;

import com.turfconnect.tournament.dto.TournamentRequest;
import com.turfconnect.tournament.dto.TournamentResponse;

import java.util.List;

public interface TournamentService {
    TournamentResponse createTournament(TournamentRequest request, String userId);
    TournamentResponse getTournament(String tournamentId);
    List<TournamentResponse> getAllTournaments();
    
    TournamentResponse openRegistration(String tournamentId, String userId);
    TournamentResponse startTournament(String tournamentId, String userId);
    TournamentResponse completeTournament(String tournamentId, String winnerTeamId, String userId);
}

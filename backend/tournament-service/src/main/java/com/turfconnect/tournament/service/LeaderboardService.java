package com.turfconnect.tournament.service;

import com.turfconnect.tournament.dto.LeaderboardEntryResponse;

import java.util.List;

public interface LeaderboardService {
    void addPoints(String tournamentId, String teamId, double points);
    List<LeaderboardEntryResponse> getLeaderboard(String tournamentId);
}

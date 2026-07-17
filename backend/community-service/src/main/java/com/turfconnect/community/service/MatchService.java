package com.turfconnect.community.service;

import com.turfconnect.community.dto.MatchRequest;
import com.turfconnect.community.dto.MatchResponse;

import java.util.List;

public interface MatchService {
    MatchResponse createMatchChallenge(MatchRequest request, String userId);
    MatchResponse acceptMatch(String matchId, String userId);
    MatchResponse rejectMatch(String matchId, String userId);
    MatchResponse cancelMatch(String matchId, String userId);
    MatchResponse completeMatch(String matchId, String winnerTeamId, String userId);
    List<MatchResponse> getMatchesForTeam(String teamId);
    MatchResponse getMatchById(String matchId);
}

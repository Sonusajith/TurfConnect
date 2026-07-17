package com.turfconnect.community.service;

import com.turfconnect.community.dto.TeamRequest;
import com.turfconnect.community.dto.TeamResponse;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(TeamRequest request, String userId);
    TeamResponse getTeamById(String teamId);
    List<TeamResponse> getAllTeams();
}

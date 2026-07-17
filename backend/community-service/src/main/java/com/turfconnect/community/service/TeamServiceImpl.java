package com.turfconnect.community.service;

import com.turfconnect.community.dto.TeamRequest;
import com.turfconnect.community.dto.TeamResponse;
import com.turfconnect.community.model.Team;
import com.turfconnect.community.model.TeamMember;
import com.turfconnect.community.model.TeamRole;
import com.turfconnect.community.model.TeamStatus;
import com.turfconnect.community.repository.TeamRepository;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Override
    public TeamResponse createTeam(TeamRequest request, String userId) {
        if (teamRepository.existsByName(request.getName())) {
            throw new BadRequestException("Team name already exists");
        }

        TeamMember creator = TeamMember.builder()
                .userId(userId)
                .role(TeamRole.CAPTAIN)
                .joinedAt(Instant.now())
                .build();

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .sportType(request.getSportType() != null ? request.getSportType() : "FOOTBALL")
                .visibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC")
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 15)
                .status(TeamStatus.ACTIVE)
                .createdBy(userId)
                .members(List.of(creator))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        team = teamRepository.save(team);
        log.info("Team created with id: {}", team.getId());
        return mapToResponse(team);
    }

    @Override
    public TeamResponse getTeamById(String teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        return mapToResponse(team);
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .logoUrl(team.getLogoUrl())
                .sportType(team.getSportType())
                .visibility(team.getVisibility())
                .maxMembers(team.getMaxMembers())
                .status(team.getStatus())
                .createdBy(team.getCreatedBy())
                .members(team.getMembers())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}

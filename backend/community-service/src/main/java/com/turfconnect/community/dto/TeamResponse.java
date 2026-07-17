package com.turfconnect.community.dto;

import com.turfconnect.community.model.TeamMember;
import com.turfconnect.community.model.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private String id;
    private String name;
    private String description;
    private String logoUrl;
    private String sportType;
    private String visibility;
    private Integer maxMembers;
    private TeamStatus status;
    private String createdBy;
    private List<TeamMember> members;
    private Instant createdAt;
    private Instant updatedAt;
}

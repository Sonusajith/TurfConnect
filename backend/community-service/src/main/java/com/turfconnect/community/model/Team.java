package com.turfconnect.community.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "teams")
public class Team {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String name;
    
    private String description;
    private String logoUrl;
    private String sportType;
    
    private String visibility; // PUBLIC, PRIVATE
    
    private Integer maxMembers;
    
    private TeamStatus status;
    
    private String createdBy; // userId of creator
    
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();
    
    private Instant createdAt;
    private Instant updatedAt;
}

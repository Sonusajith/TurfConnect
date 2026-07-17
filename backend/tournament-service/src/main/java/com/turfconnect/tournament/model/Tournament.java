package com.turfconnect.tournament.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tournaments")
public class Tournament {

    @Id
    private String id;

    private String name;
    
    private String description;

    @Indexed
    private String turfId;

    private String sportType;

    private int maxTeams;
    
    private int currentTeams;

    private BigDecimal prizePool;

    private TournamentStatus status;

    private LocalDate registrationDeadline;
    private LocalDate startDate;
    private LocalDate endDate;

    private String winnerTeamId;

    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

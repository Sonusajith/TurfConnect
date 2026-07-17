package com.turfconnect.tournament.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tournament_registrations")
@CompoundIndexes({
    @CompoundIndex(name = "tournament_team_idx", def = "{'tournamentId': 1, 'teamId': 1}", unique = true)
})
public class TournamentRegistration {

    @Id
    private String id;

    @Indexed
    private String tournamentId;

    @Indexed
    private String teamId;

    private String registeredBy;

    private RegistrationStatus status;

    private int points; // To persist leaderboard points for rebuilding

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

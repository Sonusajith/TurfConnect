package com.turfconnect.tournament.repository;

import com.turfconnect.tournament.model.TournamentRegistration;
import com.turfconnect.tournament.model.RegistrationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRegistrationRepository extends MongoRepository<TournamentRegistration, String> {
    
    boolean existsByTournamentIdAndTeamId(String tournamentId, String teamId);

    List<TournamentRegistration> findByTournamentId(String tournamentId);

    List<TournamentRegistration> findByTournamentIdAndStatus(String tournamentId, RegistrationStatus status);
}

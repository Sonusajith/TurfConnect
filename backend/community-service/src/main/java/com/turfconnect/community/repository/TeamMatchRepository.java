package com.turfconnect.community.repository;

import com.turfconnect.community.model.TeamMatch;
import com.turfconnect.community.model.MatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TeamMatchRepository extends MongoRepository<TeamMatch, String> {

    List<TeamMatch> findByHomeTeamIdOrAwayTeamId(String homeTeamId, String awayTeamId);
    
    boolean existsByBookingIdAndStatusNot(String bookingId, MatchStatus status);

    @Query("{ '$or': [ { 'homeTeamId': ?0 }, { 'awayTeamId': ?0 } ], 'date': ?1, 'status': { '$in': ['CHALLENGED', 'ACCEPTED'] }, '$and': [ { 'startTime': { '$lt': ?3 } }, { 'endTime': { '$gt': ?2 } } ] }")
    List<TeamMatch> findOverlappingMatchesForTeam(String teamId, LocalDate date, LocalTime startTime, LocalTime endTime);
}

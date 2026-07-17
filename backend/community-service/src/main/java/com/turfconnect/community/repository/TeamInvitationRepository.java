package com.turfconnect.community.repository;

import com.turfconnect.community.model.InvitationStatus;
import com.turfconnect.community.model.TeamInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamInvitationRepository extends MongoRepository<TeamInvitation, String> {
    boolean existsByTeamIdAndInviteeIdAndStatus(String teamId, String inviteeId, InvitationStatus status);
    List<TeamInvitation> findByInviteeIdAndStatus(String inviteeId, InvitationStatus status);
}

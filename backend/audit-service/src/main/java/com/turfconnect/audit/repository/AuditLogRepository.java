package com.turfconnect.audit.repository;

import com.turfconnect.audit.model.AuditLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogDocument, String> {
    Page<AuditLogDocument> findByActorId(String actorId, Pageable pageable);
    Page<AuditLogDocument> findByAction(String action, Pageable pageable);
}

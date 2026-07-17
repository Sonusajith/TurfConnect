package com.turfconnect.audit.controller;

import com.turfconnect.audit.model.AuditLogDocument;
import com.turfconnect.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    public ResponseEntity<Page<AuditLogDocument>> getAuditLogs(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        Page<AuditLogDocument> logs;
        if (actorId != null && !actorId.isEmpty()) {
            logs = auditLogRepository.findByActorId(actorId, pageRequest);
        } else if (action != null && !action.isEmpty()) {
            logs = auditLogRepository.findByAction(action, pageRequest);
        } else {
            logs = auditLogRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(logs);
    }
}

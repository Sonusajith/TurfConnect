package com.turfconnect.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLogDocument {

    @Id
    private String id;

    @Indexed
    private String actorId;

    private String actorRole;

    @Indexed
    private String action;

    private String resource;
    private String details;

    @Indexed
    private LocalDateTime timestamp;
}

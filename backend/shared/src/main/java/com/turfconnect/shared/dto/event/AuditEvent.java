package com.turfconnect.shared.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String actorId;
    private String actorRole;
    private String action;
    private String resource;
    private String details;
    private LocalDateTime timestamp;
}

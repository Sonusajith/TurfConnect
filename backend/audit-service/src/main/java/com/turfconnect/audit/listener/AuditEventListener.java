package com.turfconnect.audit.listener;

import com.turfconnect.audit.config.RabbitMQConfig;
import com.turfconnect.audit.model.AuditLogDocument;
import com.turfconnect.audit.repository.AuditLogRepository;
import com.turfconnect.shared.dto.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleAuditEvent(AuditEvent event) {
        log.info("Received audit event for action: {} by actor: {}", event.getAction(), event.getActorId());

        try {
            AuditLogDocument document = AuditLogDocument.builder()
                    .actorId(event.getActorId())
                    .actorRole(event.getActorRole())
                    .action(event.getAction())
                    .resource(event.getResource())
                    .details(event.getDetails())
                    .timestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                    .build();

            auditLogRepository.save(document);
        } catch (Exception e) {
            log.error("Failed to save audit event to MongoDB", e);
            throw e; // trigger retry or DLQ in a real prod env
        }
    }
}

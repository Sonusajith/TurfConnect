package com.turfconnect.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.turfconnect.shared.dto.event.AuditEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public AuditAspect(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterReturning(pointcut = "@annotation(com.turfconnect.shared.audit.AuditLog)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AuditLog auditLog = method.getAnnotation(AuditLog.class);

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String actorId = "SYSTEM";
            String actorRole = "UNKNOWN";

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                String roleHeader = request.getHeader("X-User-Role");
                if (userIdHeader != null) actorId = userIdHeader;
                if (roleHeader != null) actorRole = roleHeader;
            }

            String details = "";
            try {
                // Serialize request arguments (excluding request/response objects to prevent serialization errors)
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    details = objectMapper.writeValueAsString(args);
                }
            } catch (JsonProcessingException e) {
                details = "Error serializing details: " + e.getMessage();
            }

            AuditEvent event = AuditEvent.builder()
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .action(auditLog.action())
                    .resource(auditLog.resource())
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Fire and forget to RabbitMQ
            rabbitTemplate.convertAndSend("audit.exchange", "audit.routing.key", event);
            log.info("Published audit event for action: {}", auditLog.action());

        } catch (Exception e) {
            log.error("Failed to publish audit event", e);
        }
    }
}

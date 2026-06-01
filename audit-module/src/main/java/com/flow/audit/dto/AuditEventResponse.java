package com.flow.audit.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit event DTO returned to API clients.
 */
public record AuditEventResponse(
        UUID auditId,
        UUID actorId,
        String eventType,
        String resourceType,
        UUID resourceId,
        String details,
        LocalDateTime occurredAt
) {
}


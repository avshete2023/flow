package com.flow.auth.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when authentication events occur (login, logout, auth failure).
 */
public record AuthenticationAuditEvent(
        UUID userId,
        String eventType,
        String details,
        LocalDateTime occurredAt
) {
}


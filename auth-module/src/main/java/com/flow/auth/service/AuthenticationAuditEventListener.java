package com.flow.auth.service;

import com.flow.auth.event.AuthenticationAuditEvent;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Listens for authentication audit events and persists them via audit module.
 */
@Service
public class AuthenticationAuditEventListener {

    private final Optional<com.flow.audit.service.AuditService> auditService;

    public AuthenticationAuditEventListener(Optional<com.flow.audit.service.AuditService> auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onAuthenticationAuditEvent(AuthenticationAuditEvent event) {
        auditService.ifPresent(service -> service.recordSecurityEvent(
                event.userId(),
                event.eventType(),
                event.details()
        ));
    }
}



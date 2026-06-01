package com.flow.execution.service;

import com.flow.execution.event.ExecutionLifecycleAuditEvent;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Listens for execution lifecycle audit events and persists them via audit module.
 */
@Service
public class ExecutionLifecycleAuditEventListener {

    private final Optional<com.flow.audit.service.AuditService> auditService;

    public ExecutionLifecycleAuditEventListener(Optional<com.flow.audit.service.AuditService> auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onExecutionLifecycleAuditEvent(ExecutionLifecycleAuditEvent event) {
        auditService.ifPresent(service -> service.recordAdministrativeEvent(
                null,
                event.eventType(),
                "EXECUTION",
                event.executionId(),
                event.details()
        ));
    }
}



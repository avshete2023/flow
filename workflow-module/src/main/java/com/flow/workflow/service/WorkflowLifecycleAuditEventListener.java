package com.flow.workflow.service;

import com.flow.workflow.event.WorkflowLifecycleAuditEvent;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Listens for workflow lifecycle audit events and persists them via audit module.
 */
@Service
public class WorkflowLifecycleAuditEventListener {

    private final Optional<com.flow.audit.service.AuditService> auditService;

    public WorkflowLifecycleAuditEventListener(Optional<com.flow.audit.service.AuditService> auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onWorkflowLifecycleAuditEvent(WorkflowLifecycleAuditEvent event) {
        auditService.ifPresent(service -> service.recordWorkflowEvent(
                event.userId(),
                event.eventType(),
                event.workflowId(),
                event.details()
        ));
    }
}



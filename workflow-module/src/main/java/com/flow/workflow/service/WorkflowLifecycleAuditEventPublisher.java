package com.flow.workflow.service;

import com.flow.workflow.event.WorkflowLifecycleAuditEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Publishes workflow lifecycle audit events.
 */
@Service
public class WorkflowLifecycleAuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkflowLifecycleAuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishWorkflowCreatedEvent(UUID workflowId, UUID userId, String name) {
        applicationEventPublisher.publishEvent(new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_CREATED",
                "{\"name\":\"" + name + "\"}",
                LocalDateTime.now()
        ));
    }

    public void publishWorkflowUpdatedEvent(UUID workflowId, UUID userId, String changes) {
        applicationEventPublisher.publishEvent(new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_UPDATED",
                "{\"changes\":\"" + changes + "\"}",
                LocalDateTime.now()
        ));
    }

    public void publishWorkflowPublishedEvent(UUID workflowId, UUID userId, int version) {
        applicationEventPublisher.publishEvent(new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_PUBLISHED",
                "{\"version\":" + version + "}",
                LocalDateTime.now()
        ));
    }

    public void publishWorkflowActivatedEvent(UUID workflowId, UUID userId) {
        applicationEventPublisher.publishEvent(new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_ACTIVATED",
                "{}",
                LocalDateTime.now()
        ));
    }

    public void publishWorkflowDeactivatedEvent(UUID workflowId, UUID userId) {
        applicationEventPublisher.publishEvent(new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_DEACTIVATED",
                "{}",
                LocalDateTime.now()
        ));
    }

    public void publishWorkflowDeletedEvent(UUID workflowId, UUID userId) {
        applicationEventPublisher.publishEvent(new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_DELETED",
                "{}",
                LocalDateTime.now()
        ));
    }
}


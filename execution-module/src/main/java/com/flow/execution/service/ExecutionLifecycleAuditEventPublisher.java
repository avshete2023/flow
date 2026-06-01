package com.flow.execution.service;

import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.event.ExecutionLifecycleAuditEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Publishes execution lifecycle audit events.
 */
@Service
public class ExecutionLifecycleAuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ExecutionLifecycleAuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishExecutionStartedEvent(
            UUID executionId,
            UUID workflowVersionId,
            String correlationId
    ) {
        applicationEventPublisher.publishEvent(new ExecutionLifecycleAuditEvent(
                executionId,
                workflowVersionId,
                correlationId,
                "EXECUTION_START",
                ExecutionStatus.RUNNING,
                "{}",
                LocalDateTime.now()
        ));
    }

    public void publishExecutionCompletedEvent(UUID executionId, UUID workflowVersionId, String correlationId) {
        applicationEventPublisher.publishEvent(new ExecutionLifecycleAuditEvent(
                executionId,
                workflowVersionId,
                correlationId,
                "EXECUTION_COMPLETED",
                ExecutionStatus.COMPLETED,
                "{}",
                LocalDateTime.now()
        ));
    }

    public void publishExecutionFailedEvent(UUID executionId, UUID workflowVersionId, String correlationId, String errorMessage) {
        applicationEventPublisher.publishEvent(new ExecutionLifecycleAuditEvent(
                executionId,
                workflowVersionId,
                correlationId,
                "EXECUTION_FAILED",
                ExecutionStatus.FAILED,
                "{\"error\":\"" + errorMessage + "\"}",
                LocalDateTime.now()
        ));
    }

    public void publishExecutionMovedToDlqEvent(UUID executionId, UUID workflowVersionId, String correlationId, String reason) {
        applicationEventPublisher.publishEvent(new ExecutionLifecycleAuditEvent(
                executionId,
                workflowVersionId,
                correlationId,
                "EXECUTION_DLQ",
                ExecutionStatus.DLQ,
                "{\"reason\":\"" + reason + "\"}",
                LocalDateTime.now()
        ));
    }
}


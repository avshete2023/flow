package com.flow.execution.domain.entity;

import com.flow.execution.domain.model.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks one workflow execution instance.
 */
@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "workflow_version_id", nullable = false, updatable = false)
    private UUID workflowVersionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExecutionStatus status;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_context", columnDefinition = "jsonb")
    private String executionContext;

    protected WorkflowExecution() {
    }

    public WorkflowExecution(UUID id, UUID workflowVersionId, ExecutionStatus status, String correlationId, String executionContext) {
        this.id = id;
        this.workflowVersionId = workflowVersionId;
        this.status = status;
        this.correlationId = correlationId;
        this.executionContext = executionContext;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = ExecutionStatus.PENDING;
        }
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkflowVersionId() {
        return workflowVersionId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(String executionContext) {
        this.executionContext = executionContext;
    }
}


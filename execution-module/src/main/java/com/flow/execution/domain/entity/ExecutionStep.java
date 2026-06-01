package com.flow.execution.domain.entity;

import com.flow.execution.domain.model.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks individual step execution within a workflow execution.
 * Maps to node execution in the workflow DAG.
 */
@Entity
@Table(name = "execution_steps")
public class ExecutionStep {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "execution_id", nullable = false, updatable = false)
    private WorkflowExecution execution;

    @NotNull
    @Column(name = "node_id", nullable = false, updatable = false)
    private String nodeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExecutionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "step_output", columnDefinition = "jsonb")
    private String stepOutput;

    protected ExecutionStep() {
    }

    public ExecutionStep(UUID id, WorkflowExecution execution, String nodeId, ExecutionStatus status) {
        this.id = id;
        this.execution = execution;
        this.nodeId = nodeId;
        this.status = status;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = ExecutionStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public WorkflowExecution getExecution() {
        return execution;
    }

    public String getNodeId() {
        return nodeId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStepOutput() {
        return stepOutput;
    }

    public void setStepOutput(String stepOutput) {
        this.stepOutput = stepOutput;
    }
}


package com.flow.execution.service;

import com.flow.execution.domain.entity.ExecutionStep;
import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Logs execution events with structured logging for observability.
 * EXEC-016: Create Execution Event Logging
 */
@Service
public class ExecutionEventLogger {

    private static final Logger log = LoggerFactory.getLogger(ExecutionEventLogger.class);

    /**
     * Log execution status transition.
     */
    public void logExecutionStatusTransition(
            UUID executionId,
            UUID workflowVersionId,
            ExecutionStatus fromStatus,
            ExecutionStatus toStatus,
            String correlationId
    ) {
        log.info(
                "Execution status transition [executionId={}, workflowVersionId={}, from={}, to={}, correlationId={}]",
                executionId,
                workflowVersionId,
                fromStatus,
                toStatus,
                correlationId
        );
    }

    /**
     * Log execution completion.
     */
    public void logExecutionCompleted(WorkflowExecution execution) {
        long durationMs = 0;
        if (execution.getStartedAt() != null && execution.getCompletedAt() != null) {
            durationMs = java.time.temporal.ChronoUnit.MILLIS.between(
                    execution.getStartedAt(),
                    execution.getCompletedAt()
            );
        }

        log.info(
                "Execution completed [executionId={}, status={}, durationMs={}, correlationId={}]",
                execution.getId(),
                execution.getStatus(),
                durationMs,
                execution.getCorrelationId()
        );
    }

    /**
     * Log execution failure.
     */
    public void logExecutionFailed(WorkflowExecution execution) {
        log.error(
                "Execution failed [executionId={}, status={}, errorMessage={}, correlationId={}]",
                execution.getId(),
                execution.getStatus(),
                execution.getErrorMessage(),
                execution.getCorrelationId()
        );
    }

    /**
     * Log step execution started.
     */
    public void logStepStarted(ExecutionStep step, String correlationId) {
        log.debug(
                "Step execution started [executionId={}, nodeId={}, correlationId={}]",
                step.getExecution().getId(),
                step.getNodeId(),
                correlationId
        );
    }

    /**
     * Log step execution completed.
     */
    public void logStepCompleted(ExecutionStep step, String correlationId) {
        long durationMs = 0;
        if (step.getStartedAt() != null && step.getCompletedAt() != null) {
            durationMs = java.time.temporal.ChronoUnit.MILLIS.between(
                    step.getStartedAt(),
                    step.getCompletedAt()
            );
        }

        log.debug(
                "Step execution completed [executionId={}, nodeId={}, durationMs={}, correlationId={}]",
                step.getExecution().getId(),
                step.getNodeId(),
                durationMs,
                correlationId
        );
    }

    /**
     * Log step execution failure.
     */
    public void logStepFailed(ExecutionStep step, String correlationId) {
        log.warn(
                "Step execution failed [executionId={}, nodeId={}, errorMessage={}, correlationId={}]",
                step.getExecution().getId(),
                step.getNodeId(),
                step.getErrorMessage(),
                correlationId
        );
    }

    /**
     * Log execution retry attempt.
     */
    public void logRetryAttempt(
            UUID executionId,
            int retryCount,
            long delayMs,
            String correlationId
    ) {
        log.info(
                "Retry scheduled [executionId={}, retryCount={}, delayMs={}, correlationId={}]",
                executionId,
                retryCount,
                delayMs,
                correlationId
        );
    }

    /**
     * Log execution moved to DLQ.
     */
    public void logMovedToDLQ(
            UUID executionId,
            int retryCount,
            String reason,
            String correlationId
    ) {
        log.error(
                "Execution moved to DLQ [executionId={}, retryCount={}, reason={}, correlationId={}]",
                executionId,
                retryCount,
                reason,
                correlationId
        );
    }

    /**
     * Log execution timeout.
     */
    public void logExecutionTimeout(UUID executionId, long timeoutMs, String correlationId) {
        log.warn(
                "Execution timeout [executionId={}, timeoutMs={}, correlationId={}]",
                executionId,
                timeoutMs,
                correlationId
        );
    }
}


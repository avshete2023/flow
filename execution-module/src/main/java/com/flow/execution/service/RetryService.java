package com.flow.execution.service;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.queue.ExecutionPublisher;
import com.flow.execution.queue.ExecutionQueueNames;
import com.flow.execution.queue.ExecutionRequestMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Schedules retries with backoff and reroutes exhausted executions to DLQ.
 */
@Service
public class RetryService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final ExecutionRetryPolicy executionRetryPolicy;
    private final ExecutionPublisher executionPublisher;
    private final DlqService dlqService;

    public RetryService(
            WorkflowExecutionRepository workflowExecutionRepository,
            ExecutionRetryPolicy executionRetryPolicy,
            ExecutionPublisher executionPublisher,
            DlqService dlqService
    ) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionRetryPolicy = executionRetryPolicy;
        this.executionPublisher = executionPublisher;
        this.dlqService = dlqService;
    }

    @Transactional
    public RetryOutcome scheduleRetry(UUID executionId, String reason) {
        WorkflowExecution failedExecution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new ExecutionNotFoundException(executionId));

        if (failedExecution.getStatus() != ExecutionStatus.FAILED && failedExecution.getStatus() != ExecutionStatus.TIMED_OUT) {
            throw new InvalidRetryStateException(failedExecution.getStatus());
        }

        int nextAttempt = failedExecution.getRetryCount() + 1;
        if (nextAttempt > executionRetryPolicy.maxRetries()) {
            WorkflowExecution dlqExecution = dlqService.moveToDlq(executionId, reason == null ? "Retry attempts exhausted" : reason);
            return RetryOutcome.movedToDlq(dlqExecution.getId(), dlqExecution.getRetryCount());
        }

        long backoffMillis = executionRetryPolicy.backoffMillisForAttempt(nextAttempt);
        WorkflowExecution retryExecution = new WorkflowExecution(
                UUID.randomUUID(),
                failedExecution.getWorkflowVersionId(),
                ExecutionStatus.PENDING,
                failedExecution.getCorrelationId(),
                "{\"retryOfExecutionId\":\"" + failedExecution.getId() + "\",\"scheduledBackoffMillis\":" + backoffMillis + "}"
        );
        retryExecution.setRetryCount(nextAttempt);
        retryExecution.setErrorMessage(reason);
        WorkflowExecution savedRetryExecution = workflowExecutionRepository.save(retryExecution);

        executionPublisher.publishExecutionRequest(
                new ExecutionRequestMessage(
                        savedRetryExecution.getId(),
                        savedRetryExecution.getWorkflowVersionId(),
                        savedRetryExecution.getCorrelationId(),
                        LocalDateTime.now().plusNanos(backoffMillis * 1_000_000L)
                ),
                ExecutionQueueNames.WORKFLOW_RETRY_ROUTING_KEY
        );

        return RetryOutcome.scheduled(savedRetryExecution.getId(), nextAttempt, backoffMillis);
    }

    public record RetryOutcome(
            UUID executionId,
            int attempt,
            long backoffMillis,
            boolean movedToDlq
    ) {
        public static RetryOutcome scheduled(UUID executionId, int attempt, long backoffMillis) {
            return new RetryOutcome(executionId, attempt, backoffMillis, false);
        }

        public static RetryOutcome movedToDlq(UUID executionId, int attempt) {
            return new RetryOutcome(executionId, attempt, 0L, true);
        }
    }

    public static class ExecutionNotFoundException extends RuntimeException {
        public ExecutionNotFoundException(UUID executionId) {
            super("Execution not found: " + executionId);
        }
    }

    public static class InvalidRetryStateException extends RuntimeException {
        public InvalidRetryStateException(ExecutionStatus status) {
            super("Retry is only allowed for FAILED or TIMED_OUT executions, found: " + status);
        }
    }
}


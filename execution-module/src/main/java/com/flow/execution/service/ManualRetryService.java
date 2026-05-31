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
 * Handles user-triggered retries for DLQ executions.
 */
@Service
public class ManualRetryService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final ExecutionPublisher executionPublisher;

    public ManualRetryService(
            WorkflowExecutionRepository workflowExecutionRepository,
            ExecutionPublisher executionPublisher
    ) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionPublisher = executionPublisher;
    }

    @Transactional
    public WorkflowExecution manualRetry(UUID executionId) {
        WorkflowExecution dlqExecution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new ExecutionNotFoundException(executionId));

        if (dlqExecution.getStatus() != ExecutionStatus.DLQ) {
            throw new InvalidManualRetryStateException(dlqExecution.getStatus());
        }

        WorkflowExecution retryExecution = new WorkflowExecution(
                UUID.randomUUID(),
                dlqExecution.getWorkflowVersionId(),
                ExecutionStatus.PENDING,
                dlqExecution.getCorrelationId(),
                "{\"manualRetryOfExecutionId\":\"" + dlqExecution.getId() + "\"}"
        );
        retryExecution.setRetryCount(dlqExecution.getRetryCount() + 1);
        WorkflowExecution savedRetryExecution = workflowExecutionRepository.save(retryExecution);

        executionPublisher.publishExecutionRequest(
                new ExecutionRequestMessage(
                        savedRetryExecution.getId(),
                        savedRetryExecution.getWorkflowVersionId(),
                        savedRetryExecution.getCorrelationId(),
                        LocalDateTime.now()
                ),
                ExecutionQueueNames.WORKFLOW_EXECUTION_ROUTING_KEY
        );

        return savedRetryExecution;
    }

    public static class ExecutionNotFoundException extends RuntimeException {
        public ExecutionNotFoundException(UUID executionId) {
            super("Execution not found: " + executionId);
        }
    }

    public static class InvalidManualRetryStateException extends RuntimeException {
        public InvalidManualRetryStateException(ExecutionStatus status) {
            super("Manual retry is only allowed for DLQ executions, found: " + status);
        }
    }
}


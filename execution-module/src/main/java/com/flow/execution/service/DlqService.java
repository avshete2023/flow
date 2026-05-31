package com.flow.execution.service;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.queue.ExecutionPublisher;
import com.flow.execution.queue.ExecutionQueueNames;
import com.flow.execution.queue.ExecutionRequestMessage;
import com.flow.execution.state.ExecutionStateMachine;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles DLQ transitions and consumption.
 */
@Service
public class DlqService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final ExecutionStateMachine executionStateMachine;
    private final ExecutionPublisher executionPublisher;

    public DlqService(
            WorkflowExecutionRepository workflowExecutionRepository,
            ExecutionStateMachine executionStateMachine,
            ExecutionPublisher executionPublisher
    ) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionStateMachine = executionStateMachine;
        this.executionPublisher = executionPublisher;
    }

    @Transactional
    public WorkflowExecution moveToDlq(UUID executionId, String reason) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new ExecutionNotFoundException(executionId));

        executionStateMachine.assertTransitionAllowed(execution.getStatus(), ExecutionStatus.DLQ);
        execution.setStatus(ExecutionStatus.DLQ);
        execution.setCompletedAt(LocalDateTime.now());
        execution.setErrorMessage(reason == null || reason.isBlank() ? "Moved to DLQ" : reason);

        WorkflowExecution saved = workflowExecutionRepository.save(execution);

        executionPublisher.publishExecutionRequest(
                new ExecutionRequestMessage(saved.getId(), saved.getWorkflowVersionId(), saved.getCorrelationId(), LocalDateTime.now()),
                ExecutionQueueNames.WORKFLOW_DLQ_ROUTING_KEY
        );

        return saved;
    }

    @Transactional
    @RabbitListener(queues = ExecutionQueueNames.WORKFLOW_DLQ_QUEUE)
    public void consumeDlq(ExecutionRequestMessage request) {
        workflowExecutionRepository.findById(request.executionId()).ifPresent(execution -> {
            if (execution.getErrorMessage() == null || execution.getErrorMessage().isBlank()) {
                execution.setErrorMessage("Execution present in DLQ");
            }
            workflowExecutionRepository.save(execution);
        });
    }

    public static class ExecutionNotFoundException extends RuntimeException {
        public ExecutionNotFoundException(UUID executionId) {
            super("Execution not found: " + executionId);
        }
    }
}


package com.flow.execution.service;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.state.ExecutionStateMachine;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Detects and marks stale running executions as timed out.
 */
@Service
public class TimeoutService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final ExecutionStateMachine executionStateMachine;

    public TimeoutService(
            WorkflowExecutionRepository workflowExecutionRepository,
            ExecutionStateMachine executionStateMachine
    ) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionStateMachine = executionStateMachine;
    }

    @Transactional
    public int markTimedOutExecutions(Duration timeout) {
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }

        LocalDateTime threshold = LocalDateTime.now().minus(timeout);
        List<WorkflowExecution> staleExecutions = workflowExecutionRepository
                .findAllByStatusAndStartedAtBefore(ExecutionStatus.RUNNING, threshold);

        int updated = 0;
        for (WorkflowExecution execution : staleExecutions) {
            if (executionStateMachine.canTransition(execution.getStatus(), ExecutionStatus.TIMED_OUT)) {
                execution.setStatus(ExecutionStatus.TIMED_OUT);
                execution.setCompletedAt(LocalDateTime.now());
                execution.setErrorMessage("Execution timed out after " + timeout);
                workflowExecutionRepository.save(execution);
                updated++;
            }
        }

        return updated;
    }
}


package com.flow.execution.state;

import com.flow.audit.service.AuditService;
import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.event.ExecutionLifecycleAuditEvent;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Manages execution state transitions with validation and audit event generation.
 * EXEC-005: Create Execution State Service
 */
@Service
public class ExecutionStateService {

    private final ExecutionStateMachine stateMachine;
    private final ObjectProvider<AuditService> auditServiceProvider;

    public ExecutionStateService(
            ExecutionStateMachine stateMachine,
            ObjectProvider<AuditService> auditServiceProvider
    ) {
        this.stateMachine = stateMachine;
        this.auditServiceProvider = auditServiceProvider;
    }

    /**
     * Transition execution to new status with validation and audit event.
     *
     * @param execution The workflow execution to transition
     * @param newStatus The target status
     * @throws ExecutionStateMachine.InvalidExecutionStateTransitionException if transition not allowed
     */
    public void transitionStatus(WorkflowExecution execution, ExecutionStatus newStatus) {
        ExecutionStatus oldStatus = execution.getStatus();
        stateMachine.assertTransitionAllowed(oldStatus, newStatus);
        execution.setStatus(newStatus);
        publishAuditEvent(execution, oldStatus, newStatus);
    }

    /**
     * Publish audit event for status transition if audit service is available.
     */
    private void publishAuditEvent(WorkflowExecution execution, ExecutionStatus from, ExecutionStatus to) {
        AuditService auditService = auditServiceProvider.getIfAvailable();
        if (auditService != null) {
            String details = String.format("{\"from\":\"%s\",\"to\":\"%s\"}", from, to);
            auditService.recordWorkflowEvent(
                    null, // No specific actor ID for internal orchestration event
                    "EXECUTION_STATE_TRANSITION",
                    execution.getId(),
                    details
            );
        }
    }
}



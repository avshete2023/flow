package com.flow.execution.state;

import com.flow.audit.service.AuditService;
import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionStateServiceTest {

    @Mock
    private ExecutionStateMachine stateMachine;

    @Mock
    private ObjectProvider<AuditService> auditServiceProvider;

    @Mock
    private AuditService auditService;

    @Test
    void shouldTransitionStatusAndPublishAudit() {
        ExecutionStateService service = new ExecutionStateService(stateMachine, auditServiceProvider);
        UUID executionId = UUID.randomUUID();
        WorkflowExecution execution = new WorkflowExecution(
                executionId,
                UUID.randomUUID(),
                ExecutionStatus.PENDING,
                "corr-123",
                "{}"
        );

        when(auditServiceProvider.getIfAvailable()).thenReturn(auditService);

        service.transitionStatus(execution, ExecutionStatus.QUEUED);

        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.QUEUED);
        verify(stateMachine).assertTransitionAllowed(ExecutionStatus.PENDING, ExecutionStatus.QUEUED);
        verify(auditService).recordWorkflowEvent(any(), any(), any(), any());
    }

    @Test
    void shouldRejectInvalidTransition() {
        ExecutionStateService service = new ExecutionStateService(stateMachine, auditServiceProvider);
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.COMPLETED,
                "corr-123",
                "{}"
        );

        doThrow(new ExecutionStateMachine.InvalidExecutionStateTransitionException(
                ExecutionStatus.COMPLETED,
                ExecutionStatus.FAILED
        )).when(stateMachine).assertTransitionAllowed(ExecutionStatus.COMPLETED, ExecutionStatus.FAILED);

        assertThatThrownBy(() -> service.transitionStatus(execution, ExecutionStatus.FAILED))
                .isInstanceOf(ExecutionStateMachine.InvalidExecutionStateTransitionException.class);
    }

    @Test
    void shouldHandleMissingAuditService() {
        ExecutionStateService service = new ExecutionStateService(stateMachine, auditServiceProvider);
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.PENDING,
                "corr-123",
                "{}"
        );

        when(auditServiceProvider.getIfAvailable()).thenReturn(null);

        service.transitionStatus(execution, ExecutionStatus.QUEUED);

        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.QUEUED);
        // Verify that auditService is not called when provider returns null
    }
}




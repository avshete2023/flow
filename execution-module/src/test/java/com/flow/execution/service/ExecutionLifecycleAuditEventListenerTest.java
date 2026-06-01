package com.flow.execution.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.flow.audit.service.AuditService;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.event.ExecutionLifecycleAuditEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExecutionLifecycleAuditEventListenerTest {

    @Test
    void shouldListenToAndRecordExecutionLifecycleAuditEvents() {
        AuditService auditService = Mockito.mock(AuditService.class);
        ExecutionLifecycleAuditEventListener listener = new ExecutionLifecycleAuditEventListener(Optional.of(auditService));

        UUID executionId = UUID.randomUUID();
        UUID workflowVersionId = UUID.randomUUID();
        ExecutionLifecycleAuditEvent event = new ExecutionLifecycleAuditEvent(
                executionId,
                workflowVersionId,
                "corr-123",
                "EXECUTION_COMPLETED",
                ExecutionStatus.COMPLETED,
                "{}",
                LocalDateTime.now()
        );

        listener.onExecutionLifecycleAuditEvent(event);

        verify(auditService).recordAdministrativeEvent(
                eq(null),
                eq("EXECUTION_COMPLETED"),
                eq("EXECUTION"),
                eq(executionId),
                any()
        );
    }
}



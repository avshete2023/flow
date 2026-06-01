package com.flow.workflow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.flow.audit.service.AuditService;
import com.flow.workflow.event.WorkflowLifecycleAuditEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkflowLifecycleAuditEventListenerTest {

    @Test
    void shouldListenToAndRecordWorkflowLifecycleAuditEvents() {
        AuditService auditService = Mockito.mock(AuditService.class);
        WorkflowLifecycleAuditEventListener listener = new WorkflowLifecycleAuditEventListener(Optional.of(auditService));

        UUID workflowId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkflowLifecycleAuditEvent event = new WorkflowLifecycleAuditEvent(
                workflowId,
                userId,
                "WORKFLOW_CREATED",
                "{\"name\":\"test\"}",
                LocalDateTime.now()
        );

        listener.onWorkflowLifecycleAuditEvent(event);

        verify(auditService).recordWorkflowEvent(eq(userId), eq("WORKFLOW_CREATED"), eq(workflowId), any());
    }
}



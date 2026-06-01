package com.flow.workflow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.flow.workflow.event.WorkflowLifecycleAuditEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class WorkflowLifecycleAuditEventPublisherTest {

    @Test
    void shouldPublishWorkflowCreatedEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        WorkflowLifecycleAuditEventPublisher auditPublisher = new WorkflowLifecycleAuditEventPublisher(publisher);

        UUID workflowId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        auditPublisher.publishWorkflowCreatedEvent(workflowId, userId, "Test Workflow");

        verify(publisher).publishEvent(any(WorkflowLifecycleAuditEvent.class));
    }

    @Test
    void shouldPublishWorkflowPublishedEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        WorkflowLifecycleAuditEventPublisher auditPublisher = new WorkflowLifecycleAuditEventPublisher(publisher);

        UUID workflowId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        auditPublisher.publishWorkflowPublishedEvent(workflowId, userId, 1);

        verify(publisher).publishEvent(any(WorkflowLifecycleAuditEvent.class));
    }

    @Test
    void shouldPublishWorkflowActivatedAndDeactivatedEvents() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        WorkflowLifecycleAuditEventPublisher auditPublisher = new WorkflowLifecycleAuditEventPublisher(publisher);

        UUID workflowId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        auditPublisher.publishWorkflowActivatedEvent(workflowId, userId);
        auditPublisher.publishWorkflowDeactivatedEvent(workflowId, userId);

        verify(publisher, Mockito.times(2)).publishEvent(any(WorkflowLifecycleAuditEvent.class));
    }
}


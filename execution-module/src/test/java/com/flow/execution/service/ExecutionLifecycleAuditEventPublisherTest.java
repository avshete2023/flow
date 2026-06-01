package com.flow.execution.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.flow.execution.event.ExecutionLifecycleAuditEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class ExecutionLifecycleAuditEventPublisherTest {

    @Test
    void shouldPublishExecutionStartedEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        ExecutionLifecycleAuditEventPublisher auditPublisher = new ExecutionLifecycleAuditEventPublisher(publisher);

        auditPublisher.publishExecutionStartedEvent(UUID.randomUUID(), UUID.randomUUID(), "corr-123");

        verify(publisher).publishEvent(any(ExecutionLifecycleAuditEvent.class));
    }

    @Test
    void shouldPublishExecutionCompletedEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        ExecutionLifecycleAuditEventPublisher auditPublisher = new ExecutionLifecycleAuditEventPublisher(publisher);

        auditPublisher.publishExecutionCompletedEvent(UUID.randomUUID(), UUID.randomUUID(), "corr-123");

        verify(publisher).publishEvent(any(ExecutionLifecycleAuditEvent.class));
    }

    @Test
    void shouldPublishExecutionFailedEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        ExecutionLifecycleAuditEventPublisher auditPublisher = new ExecutionLifecycleAuditEventPublisher(publisher);

        auditPublisher.publishExecutionFailedEvent(UUID.randomUUID(), UUID.randomUUID(), "corr-123", "Timeout");

        verify(publisher).publishEvent(any(ExecutionLifecycleAuditEvent.class));
    }

    @Test
    void shouldPublishExecutionMovedToDlqEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        ExecutionLifecycleAuditEventPublisher auditPublisher = new ExecutionLifecycleAuditEventPublisher(publisher);

        auditPublisher.publishExecutionMovedToDlqEvent(UUID.randomUUID(), UUID.randomUUID(), "corr-123", "Retries exhausted");

        verify(publisher).publishEvent(any(ExecutionLifecycleAuditEvent.class));
    }
}


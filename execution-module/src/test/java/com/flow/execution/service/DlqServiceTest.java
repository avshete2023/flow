package com.flow.execution.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.queue.ExecutionPublisher;
import com.flow.execution.queue.ExecutionRequestMessage;
import com.flow.execution.state.ExecutionStateMachine;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DlqServiceTest {

    @Test
    void shouldMoveExecutionToDlqAndPublishMessage() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher publisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        DlqService dlqService = new DlqService(repository, new ExecutionStateMachine(), publisher);

        UUID executionId = UUID.randomUUID();
        WorkflowExecution failed = new WorkflowExecution(executionId, UUID.randomUUID(), ExecutionStatus.FAILED, "corr-dlq", "{}");
        when(repository.findById(executionId)).thenReturn(Optional.of(failed));
        when(repository.save(any(WorkflowExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowExecution moved = dlqService.moveToDlq(executionId, "retry exhausted");

        assertThat(moved.getStatus()).isEqualTo(ExecutionStatus.DLQ);
        assertThat(moved.getCompletedAt()).isNotNull();
        assertThat(moved.getErrorMessage()).isEqualTo("retry exhausted");
        verify(publisher).publishExecutionRequest(any(ExecutionRequestMessage.class), any(String.class));
    }

    @Test
    void shouldConsumeDlqMessageAndPersistMarker() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher publisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        DlqService dlqService = new DlqService(repository, new ExecutionStateMachine(), publisher);

        UUID executionId = UUID.randomUUID();
        WorkflowExecution dlq = new WorkflowExecution(executionId, UUID.randomUUID(), ExecutionStatus.DLQ, "corr", "{}");
        when(repository.findById(executionId)).thenReturn(Optional.of(dlq));

        dlqService.consumeDlq(new ExecutionRequestMessage(executionId, dlq.getWorkflowVersionId(), dlq.getCorrelationId(), LocalDateTime.now()));

        verify(repository).save(dlq);
    }

    @Test
    void shouldIgnoreUnknownDlqMessage() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher publisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        DlqService dlqService = new DlqService(repository, new ExecutionStateMachine(), publisher);

        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        dlqService.consumeDlq(new ExecutionRequestMessage(UUID.randomUUID(), UUID.randomUUID(), "corr", LocalDateTime.now()));

        verify(repository, never()).save(any());
    }
}


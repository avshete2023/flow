package com.flow.execution.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.queue.ExecutionPublisher;
import com.flow.execution.queue.ExecutionRequestMessage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetryServiceTest {

    @Test
    void shouldScheduleRetryWhenAttemptsRemain() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher executionPublisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        DlqService dlqService = org.mockito.Mockito.mock(DlqService.class);

        ExecutionRetryPolicy policy = new ExecutionRetryPolicy(3, 1_000L, 2.0d);
        RetryService retryService = new RetryService(repository, policy, executionPublisher, dlqService);

        UUID executionId = UUID.randomUUID();
        WorkflowExecution failed = new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.FAILED, "corr-1", "{}");
        failed.setRetryCount(1);

        when(repository.findById(executionId)).thenReturn(Optional.of(failed));
        when(repository.save(any(WorkflowExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RetryService.RetryOutcome outcome = retryService.scheduleRetry(executionId, "connector failed");

        assertThat(outcome.movedToDlq()).isFalse();
        assertThat(outcome.attempt()).isEqualTo(2);
        assertThat(outcome.backoffMillis()).isEqualTo(2_000L);
        verify(executionPublisher).publishExecutionRequest(any(ExecutionRequestMessage.class), any(String.class));
        verify(dlqService, never()).moveToDlq(any(), any());
    }

    @Test
    void shouldMoveExecutionToDlqWhenRetryExhausted() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher executionPublisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        DlqService dlqService = org.mockito.Mockito.mock(DlqService.class);

        ExecutionRetryPolicy policy = new ExecutionRetryPolicy(2, 1_000L, 2.0d);
        RetryService retryService = new RetryService(repository, policy, executionPublisher, dlqService);

        UUID executionId = UUID.randomUUID();
        WorkflowExecution failed = new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.TIMED_OUT, "corr-2", "{}");
        failed.setRetryCount(2);

        when(repository.findById(executionId)).thenReturn(Optional.of(failed));
        when(dlqService.moveToDlq(executionId, "retry exhausted")).thenReturn(failed);

        RetryService.RetryOutcome outcome = retryService.scheduleRetry(executionId, "retry exhausted");

        assertThat(outcome.movedToDlq()).isTrue();
        verify(dlqService).moveToDlq(executionId, "retry exhausted");
        verify(executionPublisher, never()).publishExecutionRequest(any(ExecutionRequestMessage.class), any(String.class));
    }

    @Test
    void shouldRejectRetryForInvalidStatus() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher executionPublisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        DlqService dlqService = org.mockito.Mockito.mock(DlqService.class);

        RetryService retryService = new RetryService(
                repository,
                new ExecutionRetryPolicy(3, 1_000L, 2.0d),
                executionPublisher,
                dlqService
        );

        UUID executionId = UUID.randomUUID();
        WorkflowExecution completed = new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.COMPLETED, "corr-3", "{}");
        when(repository.findById(executionId)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> retryService.scheduleRetry(executionId, "not allowed"))
                .isInstanceOf(RetryService.InvalidRetryStateException.class);
    }
}



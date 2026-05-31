package com.flow.execution.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

class ManualRetryServiceTest {

    @Test
    void shouldCreateNewExecutionForDlqManualRetry() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher publisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        ManualRetryService manualRetryService = new ManualRetryService(repository, publisher);

        UUID originalId = UUID.randomUUID();
        WorkflowExecution dlq = new WorkflowExecution(originalId, UUID.randomUUID(), ExecutionStatus.DLQ, "corr-manual", "{}");
        dlq.setRetryCount(2);

        when(repository.findById(originalId)).thenReturn(Optional.of(dlq));
        when(repository.save(any(WorkflowExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowExecution retried = manualRetryService.manualRetry(originalId);

        assertThat(retried.getId()).isNotEqualTo(originalId);
        assertThat(retried.getWorkflowVersionId()).isEqualTo(dlq.getWorkflowVersionId());
        assertThat(retried.getRetryCount()).isEqualTo(3);
        assertThat(retried.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        verify(publisher).publishExecutionRequest(any(ExecutionRequestMessage.class), any(String.class));
    }

    @Test
    void shouldRejectManualRetryForNonDlqExecution() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionPublisher publisher = org.mockito.Mockito.mock(ExecutionPublisher.class);
        ManualRetryService manualRetryService = new ManualRetryService(repository, publisher);

        UUID executionId = UUID.randomUUID();
        WorkflowExecution failed = new WorkflowExecution(executionId, UUID.randomUUID(), ExecutionStatus.FAILED, "corr", "{}");
        when(repository.findById(executionId)).thenReturn(Optional.of(failed));

        assertThatThrownBy(() -> manualRetryService.manualRetry(executionId))
                .isInstanceOf(ManualRetryService.InvalidManualRetryStateException.class);
    }
}



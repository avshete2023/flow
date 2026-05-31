package com.flow.execution.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.state.ExecutionStateMachine;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TimeoutServiceTest {

    @Test
    void shouldMarkRunningExecutionsAsTimedOut() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        ExecutionStateMachine stateMachine = new ExecutionStateMachine();
        TimeoutService timeoutService = new TimeoutService(repository, stateMachine);

        WorkflowExecution running = new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.RUNNING, "corr-timeout", "{}");
        when(repository.findAllByStatusAndStartedAtBefore(any(), any(LocalDateTime.class))).thenReturn(List.of(running));

        int updated = timeoutService.markTimedOutExecutions(Duration.ofMinutes(5));

        assertThat(updated).isEqualTo(1);
        assertThat(running.getStatus()).isEqualTo(ExecutionStatus.TIMED_OUT);
        assertThat(running.getCompletedAt()).isNotNull();
        verify(repository).save(running);
    }

    @Test
    void shouldRejectNonPositiveTimeout() {
        WorkflowExecutionRepository repository = org.mockito.Mockito.mock(WorkflowExecutionRepository.class);
        TimeoutService timeoutService = new TimeoutService(repository, new ExecutionStateMachine());

        assertThatThrownBy(() -> timeoutService.markTimedOutExecutions(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}


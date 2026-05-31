package com.flow.execution.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flow.execution.domain.model.ExecutionStatus;
import org.junit.jupiter.api.Test;

class ExecutionStateMachineTest {

    private final ExecutionStateMachine executionStateMachine = new ExecutionStateMachine();

    @Test
    void shouldAllowValidTransition() {
        assertThat(executionStateMachine.canTransition(ExecutionStatus.PENDING, ExecutionStatus.QUEUED)).isTrue();
        assertThat(executionStateMachine.canTransition(ExecutionStatus.RUNNING, ExecutionStatus.COMPLETED)).isTrue();
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThat(executionStateMachine.canTransition(ExecutionStatus.COMPLETED, ExecutionStatus.RUNNING)).isFalse();

        assertThatThrownBy(() -> executionStateMachine.assertTransitionAllowed(ExecutionStatus.COMPLETED, ExecutionStatus.RUNNING))
                .isInstanceOf(ExecutionStateMachine.InvalidExecutionStateTransitionException.class)
                .hasMessage("Invalid execution state transition from COMPLETED to RUNNING");
    }

    @Test
    void shouldRejectNullTransitionInput() {
        assertThat(executionStateMachine.canTransition(null, ExecutionStatus.QUEUED)).isFalse();
        assertThat(executionStateMachine.canTransition(ExecutionStatus.PENDING, null)).isFalse();
    }
}


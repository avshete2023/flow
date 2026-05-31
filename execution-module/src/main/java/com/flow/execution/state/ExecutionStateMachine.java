package com.flow.execution.state;

import com.flow.execution.domain.model.ExecutionStatus;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Validates execution status transitions.
 */
@Component
public class ExecutionStateMachine {

    private static final Map<ExecutionStatus, Set<ExecutionStatus>> ALLOWED_TRANSITIONS = Map.of(
            ExecutionStatus.PENDING, Set.of(ExecutionStatus.QUEUED, ExecutionStatus.FAILED),
            ExecutionStatus.QUEUED, Set.of(ExecutionStatus.RUNNING, ExecutionStatus.FAILED, ExecutionStatus.TIMED_OUT, ExecutionStatus.DLQ),
            ExecutionStatus.RUNNING, Set.of(ExecutionStatus.COMPLETED, ExecutionStatus.FAILED, ExecutionStatus.TIMED_OUT, ExecutionStatus.DLQ),
            ExecutionStatus.FAILED, Set.of(ExecutionStatus.QUEUED, ExecutionStatus.DLQ),
            ExecutionStatus.TIMED_OUT, Set.of(ExecutionStatus.QUEUED, ExecutionStatus.DLQ),
            ExecutionStatus.DLQ, Set.of(ExecutionStatus.QUEUED),
            ExecutionStatus.COMPLETED, Set.of()
    );

    public void assertTransitionAllowed(ExecutionStatus from, ExecutionStatus to) {
        if (!canTransition(from, to)) {
            throw new InvalidExecutionStateTransitionException(from, to);
        }
    }

    public boolean canTransition(ExecutionStatus from, ExecutionStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static class InvalidExecutionStateTransitionException extends RuntimeException {
        public InvalidExecutionStateTransitionException(ExecutionStatus from, ExecutionStatus to) {
            super("Invalid execution state transition from " + from + " to " + to);
        }
    }
}


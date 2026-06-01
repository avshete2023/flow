package com.flow.execution.domain.entity;

import com.flow.execution.domain.model.ExecutionStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionStepTest {

    @Test
    void shouldCreateExecutionStepWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        WorkflowExecution execution = new WorkflowExecution(
                executionId,
                UUID.randomUUID(),
                ExecutionStatus.RUNNING,
                "correlation-123",
                "{}"
        );
        String nodeId = "node-1";
        ExecutionStatus status = ExecutionStatus.PENDING;

        ExecutionStep step = new ExecutionStep(id, execution, nodeId, status);

        assertThat(step.getId()).isEqualTo(id);
        assertThat(step.getExecution()).isEqualTo(execution);
        assertThat(step.getNodeId()).isEqualTo(nodeId);
        assertThat(step.getStatus()).isEqualTo(status);
        assertThat(step.getStartedAt()).isNull();
        assertThat(step.getCompletedAt()).isNull();
    }

    @Test
    void shouldGenerateUuidOnPrePersist() {
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.RUNNING,
                "correlation-123",
                "{}"
        );

        ExecutionStep step = new ExecutionStep(null, execution, "node-1", ExecutionStatus.PENDING);
        step.prePersist();

        assertThat(step.getId()).isNotNull();
    }

    @Test
    void shouldSetDefaultStatusOnPrePersist() {
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.RUNNING,
                "correlation-123",
                "{}"
        );

        ExecutionStep step = new ExecutionStep(UUID.randomUUID(), execution, "node-1", null);
        step.prePersist();

        assertThat(step.getStatus()).isEqualTo(ExecutionStatus.PENDING);
    }

    @Test
    void shouldUpdateStatus() {
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.RUNNING,
                "correlation-123",
                "{}"
        );
        ExecutionStep step = new ExecutionStep(UUID.randomUUID(), execution, "node-1", ExecutionStatus.PENDING);

        step.setStatus(ExecutionStatus.RUNNING);

        assertThat(step.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
    }

    @Test
    void shouldSetErrorMessage() {
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.RUNNING,
                "correlation-123",
                "{}"
        );
        ExecutionStep step = new ExecutionStep(UUID.randomUUID(), execution, "node-1", ExecutionStatus.FAILED);

        step.setErrorMessage("Connection timeout");

        assertThat(step.getErrorMessage()).isEqualTo("Connection timeout");
    }

    @Test
    void shouldSetStepOutput() {
        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.RUNNING,
                "correlation-123",
                "{}"
        );
        ExecutionStep step = new ExecutionStep(UUID.randomUUID(), execution, "node-1", ExecutionStatus.COMPLETED);

        step.setStepOutput("{\"result\": \"success\"}");

        assertThat(step.getStepOutput()).isEqualTo("{\"result\": \"success\"}");
    }
}


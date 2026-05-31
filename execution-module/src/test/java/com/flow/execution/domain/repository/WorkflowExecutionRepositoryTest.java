package com.flow.execution.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class WorkflowExecutionRepositoryTest {

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Test
    void shouldPersistWorkflowExecutionWithRequiredFields() {
        UUID workflowVersionId = UUID.randomUUID();

        WorkflowExecution execution = new WorkflowExecution(
                UUID.randomUUID(),
                workflowVersionId,
                ExecutionStatus.PENDING,
                "corr-1001",
                "{\"source\":\"test\"}"
        );

        WorkflowExecution saved = workflowExecutionRepository.save(execution);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWorkflowVersionId()).isEqualTo(workflowVersionId);
        assertThat(saved.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(saved.getStartedAt()).isNotNull();
        assertThat(saved.getCompletedAt()).isNull();
        assertThat(saved.getErrorMessage()).isNull();
    }

    @Test
    void shouldFindExecutionByCorrelationId() {
        WorkflowExecution execution = workflowExecutionRepository.save(new WorkflowExecution(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ExecutionStatus.QUEUED,
                "corr-abc",
                "{}"
        ));

        assertThat(workflowExecutionRepository.findByCorrelationId("corr-abc"))
                .contains(execution);
    }
}


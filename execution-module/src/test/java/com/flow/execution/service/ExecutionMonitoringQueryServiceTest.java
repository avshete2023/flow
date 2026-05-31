package com.flow.execution.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(ExecutionMonitoringQueryService.class)
class ExecutionMonitoringQueryServiceTest {

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Autowired
    private ExecutionMonitoringQueryService executionMonitoringQueryService;

    @Test
    void shouldAggregateExecutionStatistics() {
        WorkflowExecution completed = new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.COMPLETED, "corr-1", "{}");
        completed.setRetryCount(1);
        workflowExecutionRepository.save(completed);

        workflowExecutionRepository.save(new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.FAILED, "corr-2", "{}"));
        workflowExecutionRepository.save(new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.RUNNING, "corr-3", "{}"));
        workflowExecutionRepository.save(new WorkflowExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.DLQ, "corr-4", "{}"));

        ExecutionStatistics statistics = executionMonitoringQueryService.getExecutionStatistics();

        assertThat(statistics.totalExecutions()).isEqualTo(4);
        assertThat(statistics.successfulExecutions()).isEqualTo(1);
        assertThat(statistics.failedExecutions()).isEqualTo(1);
        assertThat(statistics.runningExecutions()).isEqualTo(1);
        assertThat(statistics.dlqExecutions()).isEqualTo(1);
        assertThat(statistics.retriedExecutions()).isEqualTo(1);
    }
}


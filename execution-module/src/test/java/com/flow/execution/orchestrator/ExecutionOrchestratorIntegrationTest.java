package com.flow.execution.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorType;
import com.flow.connector.registry.ConnectorRegistry;
import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import com.flow.execution.event.ExecutionAuditEvent;
import com.flow.execution.queue.ExecutionRequestMessage;
import com.flow.execution.state.ExecutionStateMachine;
import com.flow.workflow.service.WorkflowVersionDefinitionService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@DataJpaTest
@ActiveProfiles("test")
@Import({ExecutionOrchestrator.class, ExecutionStateMachine.class, ExecutionOrchestratorIntegrationTest.TestConfig.class})
@RecordApplicationEvents
class ExecutionOrchestratorIntegrationTest {

    @Autowired
    private ExecutionOrchestrator executionOrchestrator;

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkflowVersionDefinitionService workflowVersionDefinitionService;

    @MockBean
    private ConnectorRegistry connectorRegistry;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void shouldExecuteWorkflowAndMarkExecutionCompleted() {
        UUID executionId = UUID.randomUUID();
        UUID workflowVersionId = UUID.randomUUID();
        ExecutionRequestMessage request = new ExecutionRequestMessage(executionId, workflowVersionId, "corr-808", LocalDateTime.now());

        when(workflowVersionDefinitionService.getByVersionId(workflowVersionId))
                .thenReturn(new WorkflowVersionDefinitionService.WorkflowVersionDefinition(
                        workflowVersionId,
                        UUID.randomUUID(),
                        "{\"trigger\":{\"type\":\"webhook\"},\"nodes\":[{\"id\":\"n1\",\"type\":\"log\"}],\"edges\":[]}"
                ));

        Connector connector = org.mockito.Mockito.mock(Connector.class);
        ObjectNode output = objectMapper.createObjectNode().put("result", "done");
        when(connector.execute(any(), any())).thenReturn(ConnectorExecutionResult.success(output));
        when(connectorRegistry.get(ConnectorType.LOG)).thenReturn(connector);

        executionOrchestrator.orchestrate(request);

        WorkflowExecution persisted = workflowExecutionRepository.findById(executionId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
        assertThat(persisted.getCompletedAt()).isNotNull();
        assertThat(persisted.getExecutionContext()).contains("done");

        assertThat(applicationEvents.stream(ExecutionAuditEvent.class)).hasSize(3);
    }

    @Test
    void shouldMarkExecutionFailedWhenConnectorFails() {
        UUID executionId = UUID.randomUUID();
        UUID workflowVersionId = UUID.randomUUID();

        workflowExecutionRepository.save(new WorkflowExecution(
                executionId,
                workflowVersionId,
                ExecutionStatus.PENDING,
                "corr-809",
                "{}"
        ));

        ExecutionRequestMessage request = new ExecutionRequestMessage(executionId, workflowVersionId, "corr-809", LocalDateTime.now());

        when(workflowVersionDefinitionService.getByVersionId(workflowVersionId))
                .thenReturn(new WorkflowVersionDefinitionService.WorkflowVersionDefinition(
                        workflowVersionId,
                        UUID.randomUUID(),
                        "{\"trigger\":{\"type\":\"webhook\"},\"nodes\":[{\"id\":\"n1\",\"type\":\"log\"}],\"edges\":[]}"
                ));

        Connector connector = org.mockito.Mockito.mock(Connector.class);
        when(connector.execute(any(), any())).thenReturn(ConnectorExecutionResult.failure("downstream unavailable"));
        when(connectorRegistry.get(ConnectorType.LOG)).thenReturn(connector);

        executionOrchestrator.orchestrate(request);

        WorkflowExecution persisted = workflowExecutionRepository.findById(executionId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(persisted.getErrorMessage()).contains("downstream unavailable");
        assertThat(persisted.getCompletedAt()).isNotNull();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}



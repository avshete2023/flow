package com.flow.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.workflow.domain.model.WorkflowValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkflowValidationServiceTest {

    private WorkflowValidationService workflowValidationService;

    @BeforeEach
    void setUp() {
        workflowValidationService = new WorkflowValidationService(new ObjectMapper());
    }

    @Test
    void shouldValidateWorkflowDefinitionWithConnectedDag() {
        String definitionJson = """
                {
                  "trigger": {"type": "webhook"},
                  "nodes": [
                    {"id": "start", "type": "log"},
                    {"id": "approve", "type": "conditional"},
                    {"id": "complete", "type": "http"}
                  ],
                  "edges": [
                    {"source": "start", "target": "approve"},
                    {"source": "approve", "target": "complete"}
                  ]
                }
                """;

        WorkflowValidationResult result = workflowValidationService.validateDefinition(definitionJson);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldRejectWorkflowDefinitionWithoutTrigger() {
        String definitionJson = """
                {
                  "nodes": [
                    {"id": "start", "type": "log"}
                  ],
                  "edges": []
                }
                """;

        WorkflowValidationResult result = workflowValidationService.validateDefinition(definitionJson);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Workflow trigger is required");
    }

    @Test
    void shouldRejectWorkflowDefinitionWithEmptyGraph() {
        String definitionJson = """
                {
                  "trigger": {"type": "scheduler"},
                  "nodes": [],
                  "edges": []
                }
                """;

        WorkflowValidationResult result = workflowValidationService.validateDefinition(definitionJson);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Workflow must contain at least one node");
    }

    @Test
    void shouldRejectWorkflowDefinitionWithMissingNodeFieldsAndUnknownEdgeTargets() {
        String definitionJson = """
                {
                  "trigger": {"type": "webhook"},
                  "nodes": [
                    {"id": "start"},
                    {"type": "http"}
                  ],
                  "edges": [
                    {"source": "start", "target": "missing"}
                  ]
                }
                """;

        WorkflowValidationResult result = workflowValidationService.validateDefinition(definitionJson);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Node 'start' must include type");
        assertThat(result.errors()).contains("Node at index 1 must include id");
        assertThat(result.errors()).contains("Edge target node does not exist: missing");
    }

    @Test
    void shouldRejectWorkflowDefinitionWithDisconnectedNode() {
        String definitionJson = """
                {
                  "trigger": {"type": "webhook"},
                  "nodes": [
                    {"id": "start", "type": "log"},
                    {"id": "branch", "type": "conditional"},
                    {"id": "orphan", "type": "http"}
                  ],
                  "edges": [
                    {"source": "start", "target": "branch"}
                  ]
                }
                """;

        WorkflowValidationResult result = workflowValidationService.validateDefinition(definitionJson);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Workflow graph contains disconnected or unreachable nodes: orphan");
    }

    @Test
    void shouldRejectWorkflowDefinitionWithCircularReferences() {
        String definitionJson = """
                {
                  "trigger": {"type": "webhook"},
                  "nodes": [
                    {"id": "start", "type": "log"},
                    {"id": "branch", "type": "conditional"},
                    {"id": "complete", "type": "http"}
                  ],
                  "edges": [
                    {"source": "start", "target": "branch"},
                    {"source": "branch", "target": "complete"},
                    {"source": "complete", "target": "start"}
                  ]
                }
                """;

        WorkflowValidationResult result = workflowValidationService.validateDefinition(definitionJson);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Workflow graph must contain at least one root node");
        assertThat(result.errors()).contains("Workflow graph contains circular references");
    }

    @Test
    void shouldRejectMalformedJsonDefinition() {
        WorkflowValidationResult result = workflowValidationService.validateDefinition("{invalid-json}");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Workflow definition must be valid JSON");
    }
}


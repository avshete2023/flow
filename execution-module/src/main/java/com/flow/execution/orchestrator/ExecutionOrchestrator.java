package com.flow.execution.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates workflow execution by loading the definition, traversing the graph and executing connectors.
 */
@Service
public class ExecutionOrchestrator {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowVersionDefinitionService workflowVersionDefinitionService;
    private final ConnectorRegistry connectorRegistry;
    private final ExecutionStateMachine executionStateMachine;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public ExecutionOrchestrator(
            WorkflowExecutionRepository workflowExecutionRepository,
            WorkflowVersionDefinitionService workflowVersionDefinitionService,
            ConnectorRegistry connectorRegistry,
            ExecutionStateMachine executionStateMachine,
            ApplicationEventPublisher applicationEventPublisher,
            ObjectMapper objectMapper
    ) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowVersionDefinitionService = workflowVersionDefinitionService;
        this.connectorRegistry = connectorRegistry;
        this.executionStateMachine = executionStateMachine;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void orchestrate(ExecutionRequestMessage request) {
        WorkflowExecution execution = workflowExecutionRepository.findById(request.executionId())
                .orElseGet(() -> workflowExecutionRepository.save(new WorkflowExecution(
                        request.executionId(),
                        request.workflowVersionId(),
                        ExecutionStatus.PENDING,
                        request.correlationId(),
                        "{}"
                )));

        try {
            moveToStatus(execution, ExecutionStatus.QUEUED, null);
            moveToStatus(execution, ExecutionStatus.RUNNING, null);

            WorkflowVersionDefinitionService.WorkflowVersionDefinition workflowVersion =
                    workflowVersionDefinitionService.getByVersionId(request.workflowVersionId());
            JsonNode workflowDefinition = parseWorkflowDefinition(workflowVersion.definitionJson());

            JsonNode finalPayload = executeWorkflowGraph(workflowDefinition, execution);

            execution.setExecutionContext(finalPayload.toString());
            execution.setCompletedAt(LocalDateTime.now());
            moveToStatus(execution, ExecutionStatus.COMPLETED, null);
        } catch (Exception exception) {
            execution.setCompletedAt(LocalDateTime.now());
            moveToStatus(execution, ExecutionStatus.FAILED, exception.getMessage());
        }
    }

    private JsonNode parseWorkflowDefinition(String definitionJson) {
        try {
            return objectMapper.readTree(definitionJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid workflow definition JSON", exception);
        }
    }

    private JsonNode executeWorkflowGraph(JsonNode workflowDefinition, WorkflowExecution execution) {
        JsonNode nodes = workflowDefinition.path("nodes");
        JsonNode edges = workflowDefinition.path("edges");
        if (!nodes.isArray() || nodes.isEmpty()) {
            throw new IllegalArgumentException("Workflow nodes must be a non-empty array");
        }
        if (!edges.isArray()) {
            throw new IllegalArgumentException("Workflow edges must be an array");
        }

        Map<String, JsonNode> nodeById = new LinkedHashMap<>();
        for (JsonNode node : nodes) {
            String nodeId = node.path("id").asText(null);
            if (nodeId == null || nodeId.isBlank()) {
                throw new IllegalArgumentException("Each workflow node must include id");
            }
            nodeById.put(nodeId, node);
        }

        Map<String, Set<String>> adjacency = new LinkedHashMap<>();
        Map<String, Integer> indegree = new LinkedHashMap<>();
        for (String nodeId : nodeById.keySet()) {
            adjacency.put(nodeId, new LinkedHashSet<>());
            indegree.put(nodeId, 0);
        }

        for (JsonNode edge : edges) {
            String source = edge.path("source").asText(null);
            String target = edge.path("target").asText(null);
            if (!nodeById.containsKey(source) || !nodeById.containsKey(target)) {
                throw new IllegalArgumentException("Edge references unknown node: " + source + " -> " + target);
            }
            if (adjacency.get(source).add(target)) {
                indegree.compute(target, (key, value) -> value == null ? 1 : value + 1);
            }
        }

        List<String> roots = indegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .toList();
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("Workflow must contain at least one root node");
        }

        Deque<String> queue = new ArrayDeque<>(roots);
        Set<String> visited = new LinkedHashSet<>();
        JsonNode currentPayload = createInitialPayload(execution);

        while (!queue.isEmpty()) {
            String nodeId = queue.removeFirst();
            if (!visited.add(nodeId)) {
                continue;
            }

            JsonNode node = nodeById.get(nodeId);
            ConnectorType connectorType = parseConnectorType(node.path("type").asText(null), nodeId);
            Connector connector = connectorRegistry.get(connectorType);

            JsonNode configuration = node.path("configuration").isMissingNode()
                    ? objectMapper.createObjectNode()
                    : node.path("configuration");

            ConnectorExecutionResult result = connector.execute(configuration, currentPayload);
            if (!result.success()) {
                throw new IllegalStateException("Connector '" + connectorType + "' failed at node '" + nodeId + "': " + result.errorMessage());
            }

            currentPayload = result.output() == null ? currentPayload : result.output();

            for (String next : adjacency.getOrDefault(nodeId, Set.of())) {
                queue.addLast(next);
            }
        }

        if (visited.size() != nodeById.size()) {
            Set<String> unvisited = new LinkedHashSet<>(nodeById.keySet());
            unvisited.removeAll(visited);
            throw new IllegalStateException("Workflow contains unreachable nodes: " + String.join(", ", new ArrayList<>(unvisited)));
        }

        return currentPayload;
    }

    private JsonNode createInitialPayload(WorkflowExecution execution) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("executionId", execution.getId().toString());
        payload.put("workflowVersionId", execution.getWorkflowVersionId().toString());
        if (execution.getCorrelationId() != null) {
            payload.put("correlationId", execution.getCorrelationId());
        }
        return payload;
    }

    private ConnectorType parseConnectorType(String rawType, String nodeId) {
        if (rawType == null || rawType.isBlank()) {
            throw new IllegalArgumentException("Node '" + nodeId + "' must include connector type");
        }

        try {
            return ConnectorType.valueOf(rawType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported connector type '" + rawType + "' at node '" + nodeId + "'");
        }
    }

    private void moveToStatus(WorkflowExecution execution, ExecutionStatus targetStatus, String errorMessage) {
        if (execution.getStatus() != targetStatus) {
            executionStateMachine.assertTransitionAllowed(execution.getStatus(), targetStatus);
            execution.setStatus(targetStatus);
        }

        execution.setErrorMessage(errorMessage);
        workflowExecutionRepository.save(execution);

        applicationEventPublisher.publishEvent(new ExecutionAuditEvent(
                execution.getId(),
                execution.getWorkflowVersionId(),
                execution.getCorrelationId(),
                execution.getStatus(),
                errorMessage,
                LocalDateTime.now()
        ));
    }
}



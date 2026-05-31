package com.flow.workflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.trigger.TriggerType;
import com.flow.connector.trigger.TriggerValidationModel;
import com.flow.connector.trigger.TriggerValidationService;
import com.flow.workflow.domain.model.WorkflowValidationResult;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Validates workflow definition JSON before publish or execution.
 */
@Service
public class WorkflowValidationService {

    private final ObjectMapper objectMapper;
    private final TriggerValidationService triggerValidationService;

    public WorkflowValidationService(ObjectMapper objectMapper, TriggerValidationService triggerValidationService) {
        this.objectMapper = objectMapper;
        this.triggerValidationService = triggerValidationService;
    }

    public WorkflowValidationResult validateDefinition(String definitionJson) {
        List<String> errors = new ArrayList<>();
        JsonNode root = parseDefinition(definitionJson, errors);
        if (root == null) {
            return WorkflowValidationResult.failure(errors);
        }

        JsonNode trigger = root.get("trigger");
        JsonNode nodes = root.get("nodes");
        JsonNode edges = root.get("edges");

        validateTrigger(trigger, errors);
        validateGraphContainers(nodes, edges, errors);

        if (!errors.isEmpty()) {
            return WorkflowValidationResult.failure(errors);
        }

        Map<String, JsonNode> nodeById = collectNodes(nodes, errors);
        List<Edge> parsedEdges = collectEdges(edges, nodeById, errors);

        if (!errors.isEmpty()) {
            return WorkflowValidationResult.failure(errors);
        }

        validateNodeConnectivity(nodeById, parsedEdges, errors);
        validateAcyclicGraph(nodeById.keySet(), parsedEdges, errors);

        if (errors.isEmpty()) {
            return WorkflowValidationResult.success();
        }
        return WorkflowValidationResult.failure(errors);
    }

    private JsonNode parseDefinition(String definitionJson, List<String> errors) {
        if (definitionJson == null || definitionJson.isBlank()) {
            errors.add("Workflow definition is required");
            return null;
        }

        try {
            return objectMapper.readTree(definitionJson);
        } catch (JsonProcessingException exception) {
            errors.add("Workflow definition must be valid JSON");
            return null;
        }
    }

    private void validateTrigger(JsonNode trigger, List<String> errors) {
        if (trigger == null || trigger.isNull() || !trigger.isObject() || trigger.isEmpty()) {
            errors.add("Workflow trigger is required");
            return;
        }

        String typeValue = textValue(trigger.get("type"));
        if (typeValue == null) {
            errors.add("Workflow trigger type is required");
            return;
        }

        TriggerType triggerType;
        try {
            triggerType = TriggerType.valueOf(typeValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            errors.add("Workflow trigger type is invalid: " + typeValue);
            return;
        }

        JsonNode configuration = resolveTriggerConfiguration(trigger);
        TriggerValidationModel triggerValidation = triggerValidationService.validate(triggerType, configuration);
        if (!triggerValidation.valid()) {
            errors.addAll(triggerValidation.errors());
        }
    }

    private JsonNode resolveTriggerConfiguration(JsonNode trigger) {
        JsonNode explicitConfiguration = trigger.get("configuration");
        if (explicitConfiguration != null) {
            return explicitConfiguration;
        }

        ObjectNode inlineConfiguration = ((ObjectNode) trigger).deepCopy();
        inlineConfiguration.remove("type");
        return inlineConfiguration;
    }

    private void validateGraphContainers(JsonNode nodes, JsonNode edges, List<String> errors) {
        if (nodes == null || !nodes.isArray()) {
            errors.add("Workflow nodes must be an array");
            return;
        }
        if (edges == null || !edges.isArray()) {
            errors.add("Workflow edges must be an array");
            return;
        }
        if (nodes.isEmpty()) {
            errors.add("Workflow must contain at least one node");
        }
    }

    private Map<String, JsonNode> collectNodes(JsonNode nodes, List<String> errors) {
        Map<String, JsonNode> nodeById = new LinkedHashMap<>();
        for (int index = 0; index < nodes.size(); index++) {
            JsonNode node = nodes.get(index);
            if (!node.isObject()) {
                errors.add("Node at index " + index + " must be an object");
                continue;
            }

            String nodeId = textValue(node.get("id"));
            if (nodeId == null) {
                errors.add("Node at index " + index + " must include id");
                continue;
            }
            if (nodeById.containsKey(nodeId)) {
                errors.add("Duplicate node id: " + nodeId);
                continue;
            }

            if (textValue(node.get("type")) == null) {
                errors.add("Node '" + nodeId + "' must include type");
            }

            nodeById.put(nodeId, node);
        }
        return nodeById;
    }

    private List<Edge> collectEdges(JsonNode edges, Map<String, JsonNode> nodeById, List<String> errors) {
        List<Edge> parsedEdges = new ArrayList<>();
        Set<String> uniqueEdges = new HashSet<>();

        for (int index = 0; index < edges.size(); index++) {
            JsonNode edge = edges.get(index);
            if (!edge.isObject()) {
                errors.add("Edge at index " + index + " must be an object");
                continue;
            }

            String source = textValue(edge.get("source"));
            String target = textValue(edge.get("target"));
            if (source == null || target == null) {
                errors.add("Edge at index " + index + " must include source and target");
                continue;
            }
            if (!nodeById.containsKey(source)) {
                errors.add("Edge source node does not exist: " + source);
            }
            if (!nodeById.containsKey(target)) {
                errors.add("Edge target node does not exist: " + target);
            }

            String edgeKey = source + "->" + target;
            if (!uniqueEdges.add(edgeKey)) {
                errors.add("Duplicate edge: " + edgeKey);
                continue;
            }

            parsedEdges.add(new Edge(source, target));
        }

        return parsedEdges;
    }

    private void validateNodeConnectivity(Map<String, JsonNode> nodeById, List<Edge> edges, List<String> errors) {
        if (nodeById.isEmpty()) {
            return;
        }

        Map<String, Set<String>> outgoing = new LinkedHashMap<>();
        Map<String, Integer> indegree = new LinkedHashMap<>();
        for (String nodeId : nodeById.keySet()) {
            outgoing.put(nodeId, new LinkedHashSet<>());
            indegree.put(nodeId, 0);
        }

        for (Edge edge : edges) {
            if (!outgoing.containsKey(edge.source()) || !indegree.containsKey(edge.target())) {
                continue;
            }
            if (outgoing.get(edge.source()).add(edge.target())) {
                indegree.computeIfPresent(edge.target(), (key, value) -> value + 1);
            }
        }

        List<String> rootNodes = indegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .toList();

        if (rootNodes.isEmpty()) {
            errors.add("Workflow graph must contain at least one root node");
            return;
        }

        String entryNode = rootNodes.getFirst();
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(entryNode);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (String next : outgoing.getOrDefault(current, Set.of())) {
                queue.addLast(next);
            }
        }

        List<String> disconnectedNodes = nodeById.keySet().stream()
                .filter(nodeId -> !visited.contains(nodeId))
                .sorted()
                .toList();

        if (!disconnectedNodes.isEmpty()) {
            errors.add("Workflow graph contains disconnected or unreachable nodes: " + String.join(", ", disconnectedNodes));
        }
    }

    private void validateAcyclicGraph(Set<String> nodeIds, List<Edge> edges, List<String> errors) {
        Map<String, Set<String>> adjacency = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();
        for (String nodeId : nodeIds) {
            adjacency.put(nodeId, new LinkedHashSet<>());
            indegree.put(nodeId, 0);
        }

        for (Edge edge : edges) {
            if (adjacency.containsKey(edge.source()) && adjacency.get(edge.source()).add(edge.target())) {
                indegree.computeIfPresent(edge.target(), (key, value) -> value + 1);
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.addLast(entry.getKey());
            }
        }

        int visitedCount = 0;
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            visitedCount++;
            for (String next : adjacency.getOrDefault(current, Set.of())) {
                int updated = indegree.merge(next, -1, Integer::sum);
                if (updated == 0) {
                    queue.addLast(next);
                }
            }
        }

        if (visitedCount != nodeIds.size()) {
            errors.add("Workflow graph contains circular references");
        }
    }

    private String textValue(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }

    private record Edge(String source, String target) {
    }
}





package com.flow.execution.orchestrator;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Traverses workflow DAG and sequences node execution.
 * EXEC-010: Create Workflow Graph Traversal Engine
 */
@Component
public class WorkflowGraphTraversalEngine {

    /**
     * Result of graph traversal and validation.
     */
    public static class TraversalResult {
        public final Queue<String> executionSequence;
        public final boolean isValid;
        public final String errorMessage;

        public TraversalResult(Queue<String> executionSequence, boolean isValid, String errorMessage) {
            this.executionSequence = executionSequence;
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public static TraversalResult valid(Queue<String> sequence) {
            return new TraversalResult(sequence, true, null);
        }

        public static TraversalResult invalid(String errorMessage) {
            return new TraversalResult(new ArrayDeque<>(), false, errorMessage);
        }
    }

    /**
     * Traverse workflow graph starting from trigger node.
     * Validates connectivity and detects unreachable nodes.
     *
     * @param nodes Map of nodeId -> node definition
     * @param edges Map of nodeId -> set of target nodeIds (adjacency list)
     * @param startNodeId The trigger/start node
     * @return TraversalResult with execution sequence or error
     */
    public TraversalResult traverse(Map<String, Object> nodes, Map<String, Set<String>> edges, String startNodeId) {
        // Validate inputs
        if (nodes == null || nodes.isEmpty()) {
            return TraversalResult.invalid("Workflow graph contains no nodes");
        }

        if (!nodes.containsKey(startNodeId)) {
            return TraversalResult.invalid("Start node " + startNodeId + " not found in graph");
        }

        // BFS to build execution sequence
        Queue<String> sequence = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        Queue<String> queue = new ArrayDeque<>();
        queue.add(startNodeId);
        visited.add(startNodeId);

        while (!queue.isEmpty()) {
            String currentNode = queue.poll();
            sequence.add(currentNode);

            // Add adjacent nodes
            Set<String> adjacent = edges.getOrDefault(currentNode, new HashSet<>());
            for (String nextNode : adjacent) {
                if (!nodes.containsKey(nextNode)) {
                    return TraversalResult.invalid("Edge references undefined node: " + nextNode);
                }
                if (!visited.contains(nextNode)) {
                    visited.add(nextNode);
                    queue.add(nextNode);
                }
            }
        }

        // Check for unreachable nodes
        if (visited.size() < nodes.size()) {
            Set<String> unreachable = new HashSet<>(nodes.keySet());
            unreachable.removeAll(visited);
            return TraversalResult.invalid("Workflow contains unreachable nodes: " + String.join(", ", unreachable));
        }

        return TraversalResult.valid(sequence);
    }

    /**
     * Detect circular dependencies in workflow graph.
     *
     * @param edges Adjacency list representation
     * @return true if cycle detected, false otherwise
     */
    public boolean hasCycle(Map<String, Set<String>> edges) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : edges.keySet()) {
            if (hasCycleDFS(node, edges, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleDFS(String node, Map<String, Set<String>> edges, Set<String> visited, Set<String> recursionStack) {
        visited.add(node);
        recursionStack.add(node);

        Set<String> neighbors = edges.getOrDefault(node, new HashSet<>());
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                if (hasCycleDFS(neighbor, edges, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }
}


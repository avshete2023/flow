package com.flow.execution.orchestrator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowGraphTraversalEngineTest {

    private final WorkflowGraphTraversalEngine engine = new WorkflowGraphTraversalEngine();

    @Test
    void shouldTraverseSimpleLinearGraph() {
        Map<String, Object> nodes = Map.of(
                "trigger", new Object(),
                "step1", new Object(),
                "step2", new Object(),
                "end", new Object()
        );

        Map<String, Set<String>> edges = new HashMap<>();
        edges.put("trigger", Set.of("step1"));
        edges.put("step1", Set.of("step2"));
        edges.put("step2", Set.of("end"));
        edges.put("end", new HashSet<>());

        WorkflowGraphTraversalEngine.TraversalResult result = engine.traverse(nodes, edges, "trigger");

        assertThat(result.isValid).isTrue();
        assertThat(result.executionSequence).containsExactly("trigger", "step1", "step2", "end");
    }

    @Test
    void shouldDetectUnreachableNodes() {
        Map<String, Object> nodes = Map.of(
                "trigger", new Object(),
                "step1", new Object(),
                "unreachable", new Object()
        );

        Map<String, Set<String>> edges = new HashMap<>();
        edges.put("trigger", Set.of("step1"));
        edges.put("step1", new HashSet<>());

        WorkflowGraphTraversalEngine.TraversalResult result = engine.traverse(nodes, edges, "trigger");

        assertThat(result.isValid).isFalse();
        assertThat(result.errorMessage).contains("unreachable");
    }

    @Test
    void shouldRejectMissingStartNode() {
        Map<String, Object> nodes = Map.of(
                "step1", new Object()
        );

        Map<String, Set<String>> edges = new HashMap<>();
        edges.put("step1", new HashSet<>());

        WorkflowGraphTraversalEngine.TraversalResult result = engine.traverse(nodes, edges, "nonexistent");

        assertThat(result.isValid).isFalse();
        assertThat(result.errorMessage).contains("not found");
    }

    @Test
    void shouldRejectReferenceToUndefinedNode() {
        Map<String, Object> nodes = Map.of(
                "trigger", new Object(),
                "step1", new Object()
        );

        Map<String, Set<String>> edges = new HashMap<>();
        edges.put("trigger", Set.of("step1"));
        edges.put("step1", Set.of("undefined"));

        WorkflowGraphTraversalEngine.TraversalResult result = engine.traverse(nodes, edges, "trigger");

        assertThat(result.isValid).isFalse();
        assertThat(result.errorMessage).contains("undefined");
    }

    @Test
    void shouldDetectCycle() {
        Map<String, Set<String>> edges = new HashMap<>();
        edges.put("a", Set.of("b"));
        edges.put("b", Set.of("c"));
        edges.put("c", Set.of("a"));

        boolean hasCycle = engine.hasCycle(edges);

        assertThat(hasCycle).isTrue();
    }

    @Test
    void shouldReportNoCycleForDAG() {
        Map<String, Set<String>> edges = new HashMap<>();
        edges.put("a", Set.of("b", "c"));
        edges.put("b", Set.of("d"));
        edges.put("c", Set.of("d"));
        edges.put("d", new HashSet<>());

        boolean hasCycle = engine.hasCycle(edges);

        assertThat(hasCycle).isFalse();
    }

    @Test
    void shouldReportNoCycleForEmptyGraph() {
        boolean hasCycle = engine.hasCycle(new HashMap<>());

        assertThat(hasCycle).isFalse();
    }

    @Test
    void shouldHandleEmptyNodeMap() {
        Map<String, Set<String>> edges = new HashMap<>();

        WorkflowGraphTraversalEngine.TraversalResult result = engine.traverse(new HashMap<>(), edges, "start");

        assertThat(result.isValid).isFalse();
    }
}


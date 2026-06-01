package com.flow.execution.domain.repository;

import com.flow.execution.domain.entity.ExecutionStep;
import com.flow.execution.domain.model.ExecutionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for execution step persistence.
 */
public interface ExecutionStepRepository extends JpaRepository<ExecutionStep, UUID> {

    List<ExecutionStep> findByExecutionId(UUID executionId);

    Optional<ExecutionStep> findByExecutionIdAndNodeId(UUID executionId, String nodeId);

    List<ExecutionStep> findByExecutionIdAndStatus(UUID executionId, ExecutionStatus status);

    long countByExecutionIdAndStatus(UUID executionId, ExecutionStatus status);
}


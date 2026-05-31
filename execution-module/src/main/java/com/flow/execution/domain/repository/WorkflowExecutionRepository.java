package com.flow.execution.domain.repository;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for workflow execution persistence.
 */
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

    Optional<WorkflowExecution> findByCorrelationId(String correlationId);

    List<WorkflowExecution> findAllByStatus(ExecutionStatus status);

    List<WorkflowExecution> findAllByStatusAndStartedAtBefore(ExecutionStatus status, LocalDateTime threshold);
}


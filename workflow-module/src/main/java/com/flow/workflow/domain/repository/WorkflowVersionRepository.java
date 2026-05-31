package com.flow.workflow.domain.repository;

import com.flow.workflow.domain.entity.WorkflowVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, UUID> {

    List<WorkflowVersion> findAllByWorkflowIdOrderByVersionNumberDesc(UUID workflowId);

    Optional<WorkflowVersion> findFirstByWorkflowIdOrderByVersionNumberDesc(UUID workflowId);

    boolean existsByWorkflowIdAndVersionNumber(UUID workflowId, int versionNumber);
}


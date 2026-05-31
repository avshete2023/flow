package com.flow.workflow.domain.repository;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.model.WorkflowStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    List<Workflow> findAllByOwnerId(UUID ownerId);

    List<Workflow> findAllByOwnerIdAndStatus(UUID ownerId, WorkflowStatus status);
}


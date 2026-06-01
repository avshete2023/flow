package com.flow.workflow.domain.repository;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.model.WorkflowStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    List<Workflow> findAllByOwnerId(UUID ownerId);

    List<Workflow> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId);

    List<Workflow> findAllByOwnerIdAndStatus(UUID ownerId, WorkflowStatus status);

    Optional<Workflow> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    Page<Workflow> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Page<Workflow> findAllByOwnerIdAndStatusAndDeletedAtIsNull(UUID ownerId, WorkflowStatus status, Pageable pageable);
}


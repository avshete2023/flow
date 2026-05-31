package com.flow.workflow.service;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.dto.CreateWorkflowRequest;
import com.flow.workflow.dto.UpdateWorkflowRequest;
import com.flow.workflow.dto.WorkflowResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for workflow CRUD operations.
 */
@Service
public class WorkflowCrudService {

    private final WorkflowRepository workflowRepository;

    public WorkflowCrudService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @Transactional
    public WorkflowResponse createWorkflow(UUID ownerId, CreateWorkflowRequest request) {
        Workflow workflow = new Workflow(
                UUID.randomUUID(),
                ownerId,
                request.name(),
                request.description(),
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        );

        Workflow saved = workflowRepository.save(workflow);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(UUID ownerId, UUID workflowId) {
        Workflow workflow = workflowRepository.findByIdAndOwnerIdAndDeletedAtIsNull(workflowId, ownerId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        return toResponse(workflow);
    }

    @Transactional
    public WorkflowResponse updateWorkflow(UUID ownerId, UUID workflowId, UpdateWorkflowRequest request) {
        Workflow workflow = workflowRepository.findByIdAndOwnerIdAndDeletedAtIsNull(workflowId, ownerId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        if (workflow.getStatus() != WorkflowStatus.DRAFT) {
            throw new WorkflowImmutableException(workflowId, workflow.getStatus());
        }

        workflow.setName(request.name());
        workflow.setDescription(request.description());
        workflow.setUpdatedBy(ownerId);

        Workflow saved = workflowRepository.save(workflow);
        return toResponse(saved);
    }

    @Transactional
    public void deleteWorkflow(UUID ownerId, UUID workflowId) {
        Workflow workflow = workflowRepository.findByIdAndOwnerIdAndDeletedAtIsNull(workflowId, ownerId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        workflow.setDeletedAt(LocalDateTime.now());
        workflow.setDeletedBy(ownerId);
        workflow.setUpdatedBy(ownerId);
        workflowRepository.save(workflow);
    }

    @Transactional(readOnly = true)
    public Page<WorkflowResponse> listWorkflows(UUID ownerId, WorkflowStatus status, Pageable pageable) {
        Page<Workflow> workflows = status == null
                ? workflowRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
                : workflowRepository.findAllByOwnerIdAndStatusAndDeletedAtIsNull(ownerId, status, pageable);

        return workflows.map(this::toResponse);
    }

    private WorkflowResponse toResponse(Workflow workflow) {
        return new WorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                workflow.getActiveVersionId()
        );
    }

    public static class WorkflowNotFoundException extends RuntimeException {
        public WorkflowNotFoundException(UUID workflowId) {
            super("Workflow not found: " + workflowId);
        }
    }

    public static class WorkflowImmutableException extends RuntimeException {
        public WorkflowImmutableException(UUID workflowId, WorkflowStatus status) {
            super("Workflow cannot be updated in status " + status + ": " + workflowId);
        }
    }
}


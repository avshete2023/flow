package com.flow.workflow.service;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import com.flow.workflow.dto.WorkflowResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for workflow activation lifecycle operations.
 */
@Service
public class WorkflowActivationService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;

    public WorkflowActivationService(
            WorkflowRepository workflowRepository,
            WorkflowVersionRepository workflowVersionRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowVersionRepository = workflowVersionRepository;
    }

    @Transactional
    public WorkflowResponse activateWorkflow(UUID ownerId, UUID workflowId) {
        Workflow workflow = workflowRepository.findByIdAndOwnerIdAndDeletedAtIsNull(workflowId, ownerId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        if (workflow.getStatus() != WorkflowStatus.PUBLISHED) {
            throw new InvalidWorkflowStateException("Only published workflows can be activated");
        }

        WorkflowVersion latestVersion = workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId)
                .orElseThrow(() -> new InvalidWorkflowStateException("Cannot activate workflow without a published version"));

        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setActiveVersionId(latestVersion.getId());
        workflow.setUpdatedBy(ownerId);

        Workflow saved = workflowRepository.save(workflow);
        return toResponse(saved);
    }

    @Transactional
    public WorkflowResponse deactivateWorkflow(UUID ownerId, UUID workflowId) {
        Workflow workflow = workflowRepository.findByIdAndOwnerIdAndDeletedAtIsNull(workflowId, ownerId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        if (workflow.getStatus() != WorkflowStatus.ACTIVE) {
            throw new InvalidWorkflowStateException("Only active workflows can be deactivated");
        }

        workflow.setStatus(WorkflowStatus.INACTIVE);
        workflow.setUpdatedBy(ownerId);

        Workflow saved = workflowRepository.save(workflow);
        return toResponse(saved);
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

    public static class InvalidWorkflowStateException extends RuntimeException {
        public InvalidWorkflowStateException(String message) {
            super(message);
        }
    }
}


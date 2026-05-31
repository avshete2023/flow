package com.flow.workflow.service;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.model.WorkflowValidationResult;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import com.flow.workflow.dto.PublishWorkflowResponse;
import com.flow.workflow.event.WorkflowPublishedAuditEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for publishing workflow definitions as immutable versions.
 */
@Service
public class WorkflowPublishService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowValidationService workflowValidationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public WorkflowPublishService(
            WorkflowRepository workflowRepository,
            WorkflowVersionRepository workflowVersionRepository,
            WorkflowValidationService workflowValidationService,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowVersionRepository = workflowVersionRepository;
        this.workflowValidationService = workflowValidationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public PublishWorkflowResponse publishWorkflow(UUID ownerId, UUID workflowId, String definitionJson) {
        Workflow workflow = workflowRepository.findByIdAndOwnerIdAndDeletedAtIsNull(workflowId, ownerId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        WorkflowValidationResult validationResult = workflowValidationService.validateDefinition(definitionJson);
        if (!validationResult.valid()) {
            throw new WorkflowValidationException(validationResult.errors());
        }

        int nextVersion = workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId)
                .map(existing -> existing.getVersionNumber() + 1)
                .orElse(1);

        LocalDateTime now = LocalDateTime.now();
        WorkflowVersion version = new WorkflowVersion(
                UUID.randomUUID(),
                workflowId,
                nextVersion,
                definitionJson,
                now
        );
        WorkflowVersion savedVersion = workflowVersionRepository.save(version);

        workflow.setStatus(WorkflowStatus.PUBLISHED);
        workflow.setUpdatedBy(ownerId);
        workflowRepository.save(workflow);

        WorkflowPublishedAuditEvent auditEvent = new WorkflowPublishedAuditEvent(
                workflowId,
                savedVersion.getId(),
                savedVersion.getVersionNumber(),
                ownerId,
                now
        );
        applicationEventPublisher.publishEvent(auditEvent);

        return new PublishWorkflowResponse(
                savedVersion.getId(),
                savedVersion.getVersionNumber(),
                workflow.getStatus()
        );
    }

    public static class WorkflowNotFoundException extends RuntimeException {
        public WorkflowNotFoundException(UUID workflowId) {
            super("Workflow not found: " + workflowId);
        }
    }

    public static class WorkflowValidationException extends RuntimeException {
        private final List<String> errors;

        public WorkflowValidationException(List<String> errors) {
            super("Workflow definition is invalid: " + String.join("; ", errors));
            this.errors = List.copyOf(errors);
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}


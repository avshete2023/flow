package com.flow.workflow.service;

import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides workflow version definitions to dependent modules without exposing repositories.
 */
@Service
public class WorkflowVersionDefinitionService {

    private final WorkflowVersionRepository workflowVersionRepository;

    public WorkflowVersionDefinitionService(WorkflowVersionRepository workflowVersionRepository) {
        this.workflowVersionRepository = workflowVersionRepository;
    }

    @Transactional(readOnly = true)
    public WorkflowVersionDefinition getByVersionId(UUID workflowVersionId) {
        WorkflowVersion workflowVersion = workflowVersionRepository.findById(workflowVersionId)
                .orElseThrow(() -> new WorkflowVersionNotFoundException(workflowVersionId));

        return new WorkflowVersionDefinition(
                workflowVersion.getId(),
                workflowVersion.getWorkflowId(),
                workflowVersion.getDefinitionJson()
        );
    }

    public record WorkflowVersionDefinition(
            UUID workflowVersionId,
            UUID workflowId,
            String definitionJson
    ) {
    }

    public static class WorkflowVersionNotFoundException extends RuntimeException {
        public WorkflowVersionNotFoundException(UUID workflowVersionId) {
            super("Workflow version not found: " + workflowVersionId);
        }
    }
}


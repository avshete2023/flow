package com.flow.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(WorkflowVersionDefinitionService.class)
class WorkflowVersionDefinitionServiceTest {

    @Autowired
    private WorkflowVersionDefinitionService workflowVersionDefinitionService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowVersionRepository workflowVersionRepository;

    @Test
    void shouldLoadWorkflowVersionDefinitionById() {
        UUID ownerId = UUID.randomUUID();
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Order Workflow",
                "v1",
                WorkflowStatus.PUBLISHED,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        WorkflowVersion workflowVersion = workflowVersionRepository.save(new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                1,
                "{\"trigger\":{\"type\":\"webhook\"},\"nodes\":[{\"id\":\"n1\",\"type\":\"log\"}],\"edges\":[]}",
                LocalDateTime.now()
        ));

        WorkflowVersionDefinitionService.WorkflowVersionDefinition definition =
                workflowVersionDefinitionService.getByVersionId(workflowVersion.getId());

        assertThat(definition.workflowVersionId()).isEqualTo(workflowVersion.getId());
        assertThat(definition.workflowId()).isEqualTo(workflow.getId());
        assertThat(definition.definitionJson()).contains("\"nodes\"");
    }

    @Test
    void shouldThrowWhenWorkflowVersionDoesNotExist() {
        UUID missingVersionId = UUID.randomUUID();

        assertThatThrownBy(() -> workflowVersionDefinitionService.getByVersionId(missingVersionId))
                .isInstanceOf(WorkflowVersionDefinitionService.WorkflowVersionNotFoundException.class)
                .hasMessageContaining(missingVersionId.toString());
    }
}


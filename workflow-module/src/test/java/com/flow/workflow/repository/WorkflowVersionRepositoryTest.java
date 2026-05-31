package com.flow.workflow.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class WorkflowVersionRepositoryTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowVersionRepository workflowVersionRepository;

    @Test
    void shouldPersistWorkflowVersionWithJsonDefinition() {
        Workflow workflow = workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Order Processing",
                "Workflow metadata",
                WorkflowStatus.PUBLISHED,
                null,
                null,
                null,
                null,
                null
        ));

        String definitionJson = "{\"trigger\":{},\"nodes\":[],\"edges\":[]}";
        WorkflowVersion version = new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                1,
                definitionJson,
                LocalDateTime.now()
        );

        WorkflowVersion saved = workflowVersionRepository.saveAndFlush(version);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWorkflowId()).isEqualTo(workflow.getId());
        assertThat(saved.getVersionNumber()).isEqualTo(1);
        assertThat(saved.getDefinitionJson()).isEqualTo(definitionJson);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldEnforceUniqueVersionNumberPerWorkflow() {
        Workflow workflow = workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Billing Workflow",
                null,
                WorkflowStatus.PUBLISHED,
                null,
                null,
                null,
                null,
                null
        ));

        workflowVersionRepository.saveAndFlush(new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                1,
                "{\"nodes\":[]}",
                LocalDateTime.now()
        ));

        WorkflowVersion duplicateVersion = new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                1,
                "{\"nodes\":[{\"id\":\"n1\"}]}",
                LocalDateTime.now()
        );

        assertThrows(DataIntegrityViolationException.class, () -> workflowVersionRepository.saveAndFlush(duplicateVersion));
    }

    @Test
    void shouldReturnLatestVersionForWorkflow() {
        Workflow workflow = workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Shipping Workflow",
                null,
                WorkflowStatus.PUBLISHED,
                null,
                null,
                null,
                null,
                null
        ));

        workflowVersionRepository.saveAndFlush(new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                1,
                "{\"version\":1}",
                LocalDateTime.now().minusDays(2)
        ));
        workflowVersionRepository.saveAndFlush(new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                2,
                "{\"version\":2}",
                LocalDateTime.now().minusDays(1)
        ));

        List<WorkflowVersion> versions = workflowVersionRepository.findAllByWorkflowIdOrderByVersionNumberDesc(workflow.getId());

        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getVersionNumber()).isEqualTo(2);
        assertThat(workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc(workflow.getId()))
                .isPresent()
                .get()
                .extracting(WorkflowVersion::getVersionNumber)
                .isEqualTo(2);
    }
}


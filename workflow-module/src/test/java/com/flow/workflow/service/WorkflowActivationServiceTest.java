package com.flow.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import com.flow.workflow.dto.WorkflowResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(WorkflowActivationService.class)
class WorkflowActivationServiceTest {

    @Autowired
    private WorkflowActivationService workflowActivationService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowVersionRepository workflowVersionRepository;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldActivatePublishedWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Publish Me",
                null,
                WorkflowStatus.PUBLISHED,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        WorkflowVersion version = workflowVersionRepository.save(new WorkflowVersion(
                UUID.randomUUID(),
                workflow.getId(),
                1,
                "{\"trigger\":{},\"nodes\":[],\"edges\":[]}",
                LocalDateTime.now()
        ));

        WorkflowResponse response = workflowActivationService.activateWorkflow(ownerId, workflow.getId());

        assertThat(response.status()).isEqualTo(WorkflowStatus.ACTIVE);
        assertThat(response.activeVersionId()).isEqualTo(version.getId());

        Workflow persisted = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(WorkflowStatus.ACTIVE);
        assertThat(persisted.getActiveVersionId()).isEqualTo(version.getId());
    }

    @Test
    void shouldRejectActivationForUnpublishedWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Draft Workflow",
                null,
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        assertThatThrownBy(() -> workflowActivationService.activateWorkflow(ownerId, workflow.getId()))
                .isInstanceOf(WorkflowActivationService.InvalidWorkflowStateException.class)
                .hasMessageContaining("published");
    }

    @Test
    void shouldRejectActivationForDeletedWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Deleted Workflow",
                null,
                WorkflowStatus.PUBLISHED,
                null,
                ownerId,
                ownerId,
                LocalDateTime.now(),
                ownerId
        ));

        assertThatThrownBy(() -> workflowActivationService.activateWorkflow(ownerId, workflow.getId()))
                .isInstanceOf(WorkflowActivationService.WorkflowNotFoundException.class);
    }

    @Test
    void shouldDeactivateActiveWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Active Workflow",
                null,
                WorkflowStatus.ACTIVE,
                UUID.randomUUID(),
                ownerId,
                ownerId,
                null,
                null
        ));

        WorkflowResponse response = workflowActivationService.deactivateWorkflow(ownerId, workflow.getId());

        assertThat(response.status()).isEqualTo(WorkflowStatus.INACTIVE);
        Workflow persisted = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(WorkflowStatus.INACTIVE);
    }
}


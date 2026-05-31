package com.flow.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.dto.CreateWorkflowRequest;
import com.flow.workflow.dto.UpdateWorkflowRequest;
import com.flow.workflow.dto.WorkflowResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(WorkflowCrudService.class)
class WorkflowCrudServiceTest {

    @Autowired
    private WorkflowCrudService workflowCrudService;

    @Autowired
    private WorkflowRepository workflowRepository;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldCreateWorkflowInDraftStatus() {
        WorkflowResponse response = workflowCrudService.createWorkflow(
                ownerId,
                new CreateWorkflowRequest("Order Processing", "Handles order lifecycle")
        );

        assertThat(response.workflowId()).isNotNull();
        assertThat(response.name()).isEqualTo("Order Processing");
        assertThat(response.status()).isEqualTo(WorkflowStatus.DRAFT);

        Workflow persisted = workflowRepository.findById(response.workflowId()).orElseThrow();
        assertThat(persisted.getOwnerId()).isEqualTo(ownerId);
        assertThat(persisted.getDeletedAt()).isNull();
    }

    @Test
    void shouldRetrieveWorkflowByOwnerAndId() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Billing Workflow",
                "Billing operations",
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        WorkflowResponse response = workflowCrudService.getWorkflow(ownerId, workflow.getId());

        assertThat(response.workflowId()).isEqualTo(workflow.getId());
        assertThat(response.name()).isEqualTo("Billing Workflow");
    }

    @Test
    void shouldUpdateDraftWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Old Name",
                "Old Description",
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        WorkflowResponse response = workflowCrudService.updateWorkflow(
                ownerId,
                workflow.getId(),
                new UpdateWorkflowRequest("New Name", "New Description")
        );

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New Description");

        Workflow persisted = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("New Name");
        assertThat(persisted.getDescription()).isEqualTo("New Description");
    }

    @Test
    void shouldRejectUpdateForPublishedWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Published Workflow",
                "Locked",
                WorkflowStatus.PUBLISHED,
                UUID.randomUUID(),
                ownerId,
                ownerId,
                null,
                null
        ));

        assertThatThrownBy(() -> workflowCrudService.updateWorkflow(
                ownerId,
                workflow.getId(),
                new UpdateWorkflowRequest("Attempted Update", "Should fail")
        ))
                .isInstanceOf(WorkflowCrudService.WorkflowImmutableException.class)
                .hasMessageContaining("cannot be updated");
    }

    @Test
    void shouldSoftDeleteWorkflow() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Delete Me",
                "Soon removed",
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        workflowCrudService.deleteWorkflow(ownerId, workflow.getId());

        Workflow persisted = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertThat(persisted.getDeletedAt()).isNotNull();
        assertThat(persisted.getDeletedBy()).isEqualTo(ownerId);

        assertThatThrownBy(() -> workflowCrudService.getWorkflow(ownerId, workflow.getId()))
                .isInstanceOf(WorkflowCrudService.WorkflowNotFoundException.class);
    }

    @Test
    void shouldListWorkflowsWithPaginationAndStatusFilter() {
        workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Draft A",
                null,
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));
        workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Draft B",
                null,
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));
        workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Published A",
                null,
                WorkflowStatus.PUBLISHED,
                UUID.randomUUID(),
                ownerId,
                ownerId,
                null,
                null
        ));
        workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Deleted Draft",
                null,
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                java.time.LocalDateTime.now(),
                ownerId
        ));

        Page<WorkflowResponse> pageAll = workflowCrudService.listWorkflows(ownerId, null, PageRequest.of(0, 2));
        Page<WorkflowResponse> pageDraft = workflowCrudService.listWorkflows(ownerId, WorkflowStatus.DRAFT, PageRequest.of(0, 10));

        assertThat(pageAll.getTotalElements()).isEqualTo(3);
        assertThat(pageAll.getContent()).hasSize(2);
        assertThat(pageDraft.getTotalElements()).isEqualTo(2);
        assertThat(pageDraft.getContent()).extracting(WorkflowResponse::status).containsOnly(WorkflowStatus.DRAFT);
    }

    @Test
    void shouldRejectAccessToWorkflowOwnedByAnotherUser() {
        UUID anotherOwnerId = UUID.randomUUID();
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                anotherOwnerId,
                "Other Owner Workflow",
                null,
                WorkflowStatus.DRAFT,
                null,
                anotherOwnerId,
                anotherOwnerId,
                null,
                null
        ));

        assertThatThrownBy(() -> workflowCrudService.getWorkflow(ownerId, workflow.getId()))
                .isInstanceOf(WorkflowCrudService.WorkflowNotFoundException.class);
    }
}


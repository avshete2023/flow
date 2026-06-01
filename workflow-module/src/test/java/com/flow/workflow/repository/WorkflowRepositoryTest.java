package com.flow.workflow.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.repository.WorkflowRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class WorkflowRepositoryTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistWorkflowWithAuditFieldsAndDefaultDraftStatus() {
        UUID ownerId = UUID.randomUUID();

        Workflow workflow = new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Order Processing",
                "Workflow for processing orders",
                null,
                null,
                null,
                null,
                null,
                null
        );

        Workflow saved = workflowRepository.saveAndFlush(workflow);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
        assertThat(saved.getStatus()).isEqualTo(WorkflowStatus.DRAFT);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindWorkflowsByOwnerAndStatus() {
        UUID ownerId = UUID.randomUUID();

        workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Draft Workflow",
                null,
                WorkflowStatus.DRAFT,
                null,
                null,
                null,
                null,
                null
        ));

        workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Active Workflow",
                null,
                WorkflowStatus.ACTIVE,
                UUID.randomUUID(),
                null,
                null,
                null,
                null
        ));

        List<Workflow> ownerWorkflows = workflowRepository.findAllByOwnerId(ownerId);
        List<Workflow> activeWorkflows = workflowRepository.findAllByOwnerIdAndStatus(ownerId, WorkflowStatus.ACTIVE);

        assertThat(ownerWorkflows).hasSize(2);
        assertThat(activeWorkflows).hasSize(1);
        assertThat(activeWorkflows.get(0).getName()).isEqualTo("Active Workflow");
    }

    @Test
    void shouldExcludeSoftDeletedWorkflowsInOwnerLookup() {
        UUID ownerId = UUID.randomUUID();

        workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Active Workflow",
                null,
                WorkflowStatus.DRAFT,
                null,
                null,
                null,
                null,
                null
        ));

        workflowRepository.saveAndFlush(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Deleted Workflow",
                null,
                WorkflowStatus.DRAFT,
                null,
                null,
                null,
                LocalDateTime.now(),
                UUID.randomUUID()
        ));

        List<Workflow> activeOwnerWorkflows = workflowRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId);

        assertThat(activeOwnerWorkflows).hasSize(1);
        assertThat(activeOwnerWorkflows.get(0).getName()).isEqualTo("Active Workflow");
    }

    @Test
    void shouldRequireMandatoryFields() {
        Workflow invalidWorkflow = new Workflow(
                UUID.randomUUID(),
                null,
                null,
                null,
                WorkflowStatus.DRAFT,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ConstraintViolationException.class, () -> {
            workflowRepository.save(invalidWorkflow);
            workflowRepository.flush();
            entityManager.clear();
        });
    }
}


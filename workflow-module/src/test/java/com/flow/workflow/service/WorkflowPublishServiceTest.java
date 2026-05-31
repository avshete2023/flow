package com.flow.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.flow.workflow.domain.entity.Workflow;
import com.flow.workflow.domain.entity.WorkflowVersion;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.domain.model.WorkflowValidationResult;
import com.flow.workflow.domain.repository.WorkflowRepository;
import com.flow.workflow.domain.repository.WorkflowVersionRepository;
import com.flow.workflow.dto.PublishWorkflowResponse;
import com.flow.workflow.event.WorkflowPublishedAuditEvent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@DataJpaTest
@ActiveProfiles("test")
@Import(WorkflowPublishService.class)
@RecordApplicationEvents
class WorkflowPublishServiceTest {

    @Autowired
    private WorkflowPublishService workflowPublishService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowVersionRepository workflowVersionRepository;

    @MockBean
    private WorkflowValidationService workflowValidationService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldPublishWorkflowAfterSuccessfulValidationAndCreateImmutableVersion() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Order Processing",
                "Initial draft",
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        String v1Definition = "{\"trigger\":{\"type\":\"webhook\"},\"nodes\":[{\"id\":\"n1\",\"type\":\"log\"}],\"edges\":[]}";
        String v2Definition = "{\"trigger\":{\"type\":\"webhook\"},\"nodes\":[{\"id\":\"n1\",\"type\":\"log\"},{\"id\":\"n2\",\"type\":\"http\"}],\"edges\":[{\"source\":\"n1\",\"target\":\"n2\"}]}";

        when(workflowValidationService.validateDefinition(any())).thenReturn(WorkflowValidationResult.success());

        PublishWorkflowResponse first = workflowPublishService.publishWorkflow(ownerId, workflow.getId(), v1Definition);
        PublishWorkflowResponse second = workflowPublishService.publishWorkflow(ownerId, workflow.getId(), v2Definition);

        assertThat(first.version()).isEqualTo(1);
        assertThat(second.version()).isEqualTo(2);
        assertThat(second.status()).isEqualTo(WorkflowStatus.PUBLISHED);

        List<WorkflowVersion> versions = workflowVersionRepository.findAllByWorkflowIdOrderByVersionNumberDesc(workflow.getId());
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getVersionNumber()).isEqualTo(2);
        assertThat(versions.get(0).getDefinitionJson()).isEqualTo(v2Definition);
        assertThat(versions.get(1).getVersionNumber()).isEqualTo(1);
        assertThat(versions.get(1).getDefinitionJson()).isEqualTo(v1Definition);

        Workflow persistedWorkflow = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertThat(persistedWorkflow.getStatus()).isEqualTo(WorkflowStatus.PUBLISHED);
        assertThat(applicationEvents.stream(WorkflowPublishedAuditEvent.class)).hasSize(2);
    }

    @Test
    void shouldRejectPublishWhenValidationFails() {
        Workflow workflow = workflowRepository.save(new Workflow(
                UUID.randomUUID(),
                ownerId,
                "Invalid Workflow",
                "Draft",
                WorkflowStatus.DRAFT,
                null,
                ownerId,
                ownerId,
                null,
                null
        ));

        when(workflowValidationService.validateDefinition(any()))
                .thenReturn(WorkflowValidationResult.failure(List.of("Workflow trigger is required")));

        assertThatThrownBy(() -> workflowPublishService.publishWorkflow(ownerId, workflow.getId(), "{}"))
                .isInstanceOf(WorkflowPublishService.WorkflowValidationException.class)
                .hasMessageContaining("invalid");

        assertThat(workflowVersionRepository.findAllByWorkflowIdOrderByVersionNumberDesc(workflow.getId())).isEmpty();
        Workflow persistedWorkflow = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertThat(persistedWorkflow.getStatus()).isEqualTo(WorkflowStatus.DRAFT);
        assertThat(applicationEvents.stream(WorkflowPublishedAuditEvent.class)).isEmpty();
    }
}




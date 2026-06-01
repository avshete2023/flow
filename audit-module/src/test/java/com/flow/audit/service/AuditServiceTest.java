package com.flow.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.audit.domain.entity.AuditEvent;
import com.flow.audit.domain.repository.AuditEventRepository;
import com.flow.audit.dto.AuditEventResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AuditServiceTest {

    @Test
    void shouldRecordSecurityWorkflowAndAdministrativeEvents() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditService auditService = new AuditService(repository);

        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditEventResponse security = auditService.recordSecurityEvent(UUID.randomUUID(), "LOGIN", "{}");
        AuditEventResponse workflow = auditService.recordWorkflowEvent(
                UUID.randomUUID(),
                "WORKFLOW_PUBLISHED",
                UUID.randomUUID(),
                "{}"
        );
        AuditEventResponse admin = auditService.recordAdministrativeEvent(
                UUID.randomUUID(),
                "USER_ROLE_UPDATED",
                "ADMINISTRATION",
                UUID.randomUUID(),
                "{}"
        );

        assertThat(security.resourceType()).isEqualTo("SECURITY");
        assertThat(workflow.resourceType()).isEqualTo("WORKFLOW");
        assertThat(admin.resourceType()).isEqualTo("ADMINISTRATION");
    }

    @Test
    void shouldListAndRetrieveAuditEvents() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditService auditService = new AuditService(repository);

        UUID auditId = UUID.randomUUID();
        AuditEvent event = new AuditEvent(
                auditId,
                UUID.randomUUID(),
                "EXECUTION_FAILED",
                "WORKFLOW",
                UUID.randomUUID(),
                "{\"error\":\"timeout\"}",
                LocalDateTime.now()
        );

        when(repository.findByFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(event), PageRequest.of(0, 20), 1));
        when(repository.findById(auditId)).thenReturn(Optional.of(event));

        Page<AuditEventResponse> page = auditService.listAuditEvents(
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20)
        );
        AuditEventResponse found = auditService.getAuditEvent(auditId);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().auditId()).isEqualTo(auditId);
        assertThat(found.eventType()).isEqualTo("EXECUTION_FAILED");
        verify(repository).findById(auditId);
    }

    @Test
    void shouldThrowWhenAuditEventIsMissing() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditService auditService = new AuditService(repository);

        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditService.getAuditEvent(missingId))
                .isInstanceOf(AuditService.AuditEventNotFoundException.class);
    }
}



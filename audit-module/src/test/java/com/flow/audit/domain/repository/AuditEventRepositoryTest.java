package com.flow.audit.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.audit.domain.entity.AuditEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AuditEventRepositoryTest {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void shouldPersistAuditEventWithRequiredFields() {
        AuditEvent saved = auditEventRepository.save(new AuditEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "LOGIN",
                "SECURITY",
                null,
                "{\"ip\":\"127.0.0.1\"}",
                LocalDateTime.now()
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventType()).isEqualTo("LOGIN");
        assertThat(saved.getResourceType()).isEqualTo("SECURITY");
        assertThat(saved.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldFilterAuditEventsByActorAndEventType() {
        UUID actorA = UUID.randomUUID();
        UUID actorB = UUID.randomUUID();

        auditEventRepository.save(new AuditEvent(
                UUID.randomUUID(),
                actorA,
                "WORKFLOW_PUBLISHED",
                "WORKFLOW",
                UUID.randomUUID(),
                "{}",
                LocalDateTime.now().minusHours(2)
        ));
        auditEventRepository.save(new AuditEvent(
                UUID.randomUUID(),
                actorB,
                "LOGIN",
                "SECURITY",
                null,
                "{}",
                LocalDateTime.now().minusHours(1)
        ));

        Page<AuditEvent> filtered = auditEventRepository.findByFilters(
                actorA,
                "WORKFLOW_PUBLISHED",
                null,
                null,
                null,
                PageRequest.of(0, 20)
        );

        assertThat(filtered.getTotalElements()).isEqualTo(1);
        assertThat(filtered.getContent().getFirst().getActorId()).isEqualTo(actorA);
        assertThat(filtered.getContent().getFirst().getEventType()).isEqualTo("WORKFLOW_PUBLISHED");
    }
}


package com.flow.audit.service;

import com.flow.audit.domain.entity.AuditEvent;
import com.flow.audit.domain.repository.AuditEventRepository;
import com.flow.audit.dto.AuditEventResponse;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles audit persistence and read APIs for filtered queries.
 */
@Service
public class AuditService {

    private static final String RESOURCE_SECURITY = "SECURITY";
    private static final String RESOURCE_WORKFLOW = "WORKFLOW";
    private static final String RESOURCE_ADMINISTRATION = "ADMINISTRATION";

    private final AuditEventRepository auditEventRepository;

    public AuditService(ObjectProvider<AuditEventRepository> repositoryProvider) {
        this.auditEventRepository = repositoryProvider.getIfAvailable();
    }

    AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public AuditEventResponse recordSecurityEvent(UUID actorId, String eventType, String detailsJson) {
        return toResponse(createAndSave(actorId, eventType, RESOURCE_SECURITY, null, detailsJson));
    }

    @Transactional
    public AuditEventResponse recordWorkflowEvent(UUID actorId, String eventType, UUID resourceId, String detailsJson) {
        return toResponse(createAndSave(actorId, eventType, RESOURCE_WORKFLOW, resourceId, detailsJson));
    }

    @Transactional
    public AuditEventResponse recordAdministrativeEvent(
            UUID actorId,
            String eventType,
            String resourceType,
            UUID resourceId,
            String detailsJson
    ) {
        return toResponse(createAndSave(actorId, eventType, normalize(resourceType, RESOURCE_ADMINISTRATION), resourceId, detailsJson));
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> listAuditEvents(
            UUID actorId,
            String eventType,
            String resourceType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return requireRepository()
                .findByFilters(actorId, eventType, resourceType, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AuditEventResponse getAuditEvent(UUID auditId) {
        return requireRepository()
                .findById(auditId)
                .map(this::toResponse)
                .orElseThrow(() -> new AuditEventNotFoundException(auditId));
    }

    private AuditEvent createAndSave(
            UUID actorId,
            String eventType,
            String resourceType,
            UUID resourceId,
            String detailsJson
    ) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                actorId,
                required(eventType, "eventType"),
                required(resourceType, "resourceType"),
                resourceId,
                normalize(detailsJson, "{}"),
                LocalDateTime.now()
        );
        return requireRepository().save(event);
    }

    private AuditEventRepository requireRepository() {
        if (auditEventRepository == null) {
            throw new IllegalStateException("Audit persistence is not available in this runtime context");
        }
        return auditEventRepository;
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String required(String value, String field) {
        String normalized = normalize(value, null);
        if (normalized == null) {
            throw new IllegalArgumentException((field == null ? "field" : field).toLowerCase(Locale.ROOT) + " is required");
        }
        return normalized;
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getActorId(),
                event.getEventType(),
                event.getResourceType(),
                event.getResourceId(),
                event.getDetailsJson(),
                event.getOccurredAt()
        );
    }

    public static class AuditEventNotFoundException extends RuntimeException {
        public AuditEventNotFoundException(UUID auditId) {
            super("Audit event not found: " + Objects.requireNonNull(auditId));
        }
    }
}



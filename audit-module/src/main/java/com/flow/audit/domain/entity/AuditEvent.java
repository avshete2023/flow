package com.flow.audit.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record for security, workflow, and administrative events.
 */
@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_id")
    private UUID actorId;

    @NotBlank
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @NotBlank
    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "details_json", nullable = false, columnDefinition = "jsonb")
    private String detailsJson;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(
            UUID id,
            UUID actorId,
            String eventType,
            String resourceType,
            UUID resourceId,
            String detailsJson,
            LocalDateTime occurredAt
    ) {
        this.id = id;
        this.actorId = actorId;
        this.eventType = eventType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.detailsJson = detailsJson;
        this.occurredAt = occurredAt;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        if (detailsJson == null || detailsJson.isBlank()) {
            detailsJson = "{}";
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}


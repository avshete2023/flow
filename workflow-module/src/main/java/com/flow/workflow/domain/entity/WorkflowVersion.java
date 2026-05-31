package com.flow.workflow.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable snapshot of a workflow definition at publish time.
 */
@Entity
@Table(
        name = "workflow_versions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workflow_versions_workflow_version", columnNames = {"workflow_id", "version_number"})
        }
)
public class WorkflowVersion {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "workflow_id", nullable = false, updatable = false)
    private UUID workflowId;

    @Min(1)
    @Column(name = "version_number", nullable = false, updatable = false)
    private int versionNumber;

    @NotBlank
    @Column(name = "definition_json", nullable = false, columnDefinition = "jsonb", updatable = false)
    private String definitionJson;

    @Column(name = "published_at", updatable = false)
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected WorkflowVersion() {
    }

    public WorkflowVersion(
            UUID id,
            UUID workflowId,
            int versionNumber,
            String definitionJson,
            LocalDateTime publishedAt
    ) {
        this.id = id;
        this.workflowId = workflowId;
        this.versionNumber = versionNumber;
        this.definitionJson = definitionJson;
        this.publishedAt = publishedAt;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkflowId() {
        return workflowId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getDefinitionJson() {
        return definitionJson;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}


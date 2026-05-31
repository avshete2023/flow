CREATE TABLE workflow_versions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    definition_json JSONB NOT NULL,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_versions_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES workflows (id),
    CONSTRAINT uk_workflow_versions_workflow_version
        UNIQUE (workflow_id, version_number)
);

CREATE INDEX idx_workflow_version ON workflow_versions (workflow_id, version_number);


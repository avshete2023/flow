CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    active_version_id UUID,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP NOT NULL,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

CREATE INDEX idx_workflow_owner ON workflows (owner_id);
CREATE INDEX idx_workflow_status ON workflows (status);


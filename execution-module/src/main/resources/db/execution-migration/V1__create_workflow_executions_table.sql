CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY,
    workflow_version_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(100),
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    execution_context JSONB
);

CREATE INDEX idx_execution_status ON workflow_executions (status);
CREATE INDEX idx_execution_workflow ON workflow_executions (workflow_version_id);
CREATE INDEX idx_execution_started ON workflow_executions (started_at);
CREATE INDEX idx_execution_correlation ON workflow_executions (correlation_id);


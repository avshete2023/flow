CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_id UUID,
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    details_json JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_actor ON audit_events(actor_id);
CREATE INDEX idx_audit_event ON audit_events(event_type);
CREATE INDEX idx_audit_resource ON audit_events(resource_type, resource_id);
CREATE INDEX idx_audit_occurred ON audit_events(occurred_at);


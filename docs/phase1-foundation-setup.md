# Phase 1 Foundation Setup

## Maven Structure

```
flow-api/
|- pom.xml (parent aggregator)
|- flow-api/
|- auth-module/
|- user-module/
|- workflow-module/
|- execution-module/
|- connector-module/
|- monitoring-module/
|- audit-module/
`- shared-module/
```

## Dependency Diagram

```mermaid
flowchart TD
  FLOW_API[flow-api] --> AUTH[auth-module]
  FLOW_API --> USER[user-module]
  FLOW_API --> WORKFLOW[workflow-module]
  FLOW_API --> EXEC[execution-module]
  FLOW_API --> CONNECTOR[connector-module]
  FLOW_API --> MONITORING[monitoring-module]
  FLOW_API --> AUDIT[audit-module]
  FLOW_API --> SHARED[shared-module]

  AUTH --> USER
  WORKFLOW --> SHARED
  EXEC --> WORKFLOW
  EXEC --> CONNECTOR
  MONITORING --> EXEC
  CONNECTOR --> SHARED
  AUDIT --> SHARED
```

## Notes

- No entities, APIs, or business logic are introduced in this phase.
- `flow-api` remains the only executable Spring Boot application module.
- External dependencies are configured via environment variables.


System Architecture
===================

Version: 1.0

Status: Approved Architecture Baseline

Parent Documents:

*   docs/00-project-charter.md
    
*   docs/01-product-requirements-document.md
    
*   docs/02-user-stories.md
    

1\. Architecture Overview
=========================

Flow is implemented as a Modular Monolith that provides workflow orchestration, execution management, monitoring, auditing, and extensibility through a connector framework.

The architecture is intentionally designed to:

*   Maximize maintainability
    
*   Support AI-assisted development
    
*   Demonstrate enterprise-grade patterns
    
*   Avoid premature microservice complexity
    
*   Allow future extraction of services if required
    

2\. Architectural Style
=======================

Selected Style
--------------

Modular Monolith

Rationale
---------

### Benefits

*   Easier development for solo developer
    
*   Simpler debugging
    
*   Simpler deployment
    
*   Strong module boundaries
    
*   Easier testing
    

### Risks

*   Boundary erosion
    
*   Accidental coupling
    

### Mitigation

*   Module ownership rules
    
*   Architecture guardrails
    
*   Package restrictions
    
*   ADR governance
    

3\. Architecture Principles
===========================

AP-001
------

Business capabilities belong to exactly one module.

AP-002
------

Controllers contain no business logic.

AP-003
------

Repositories remain private to owning module.

AP-004
------

Communication occurs through services and events.

AP-005
------

Workflow execution is asynchronous.

AP-006
------

All external communication is abstracted behind connectors.

AP-007
------

Every state transition is auditable.

4\. High-Level Architecture
===========================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   flowchart LR  UI[React UI]  API[Spring Boot API]  AUTH[Auth Module]  USER[User Module]  WF[Workflow Module]  EXEC[Execution Module]  CONN[Connector Module]  MON[Monitoring Module]  AUD[Audit Module]  DB[(PostgreSQL)]  REDIS[(Redis)]  MQ[(RabbitMQ)]  UI --> API  API --> AUTH  API --> USER  API --> WF  API --> EXEC  API --> MON  API --> AUD  WF --> DB  AUTH --> DB  USER --> DB  EXEC --> DB  AUD --> DB  EXEC --> MQ  CONN --> REDIS  EXEC --> CONN  MON --> DB  EXEC --> AUD  AUTH --> AUD  WF --> AUD   `

5\. Module Architecture
=======================

auth-module
-----------

### Responsibilities

*   Login
    
*   Registration
    
*   JWT
    
*   Refresh Tokens
    
*   Security Filters
    

### Owned Components

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   controller  service  security  dto   `

### Does Not Own

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   workflow logic  execution logic   `

user-module
-----------

### Responsibilities

*   User profile
    
*   User management
    
*   Roles
    

### Owned Entities

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   User  Role   `

workflow-module
---------------

### Responsibilities

*   Workflow lifecycle
    
*   Workflow validation
    
*   Workflow publishing
    
*   Workflow versioning
    

### Owned Entities

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   Workflow  WorkflowVersion   `

execution-module
----------------

### Responsibilities

*   Workflow execution
    
*   State machine
    
*   Retry
    
*   Timeout
    
*   DLQ
    
*   Resume
    

### Owned Entities

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   WorkflowExecution  ExecutionStep   `

connector-module
----------------

### Responsibilities

*   Trigger framework
    
*   Connector framework
    
*   HTTP connector
    
*   Delay connector
    
*   Conditional connector
    
*   Log connector
    

### Owned Components

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   ConnectorRegistry  TriggerRegistry   `

monitoring-module
-----------------

### Responsibilities

*   Dashboard
    
*   Metrics
    
*   Health
    

audit-module
------------

### Responsibilities

*   Audit logging
    
*   Audit querying
    

notification-module
-------------------

Reserved for future implementation.

shared-module
-------------

### Responsibilities

*   Exceptions
    
*   Constants
    
*   Utilities
    
*   Shared DTOs
    

6\. Dependency Rules
====================

Allowed
-------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   flowchart TD  AUTH --> USER  WF --> SHARED  EXEC --> WF  EXEC --> CONN  MON --> EXEC  AUD --> SHARED  CONN --> SHARED   `

Forbidden
---------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   workflow-module -> execution-module  shared-module -> workflow-module  shared-module -> execution-module  monitoring-module -> workflow-module repository   `

7\. Layered Architecture
========================

Each module follows:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   flowchart TD  CONTROLLER  SERVICE  DOMAIN  REPOSITORY  DB[(Database)]  CONTROLLER --> SERVICE  SERVICE --> DOMAIN  SERVICE --> REPOSITORY  REPOSITORY --> DB   `

8\. Workflow Lifecycle
======================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   stateDiagram-v2  [*] --> DRAFT  DRAFT --> PUBLISHED  PUBLISHED --> ACTIVE  ACTIVE --> INACTIVE  INACTIVE --> ACTIVE  ACTIVE --> ARCHIVED  INACTIVE --> ARCHIVED   `

9\. Workflow Execution Lifecycle
================================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   stateDiagram-v2  [*] --> PENDING  PENDING --> QUEUED  QUEUED --> RUNNING  RUNNING --> COMPLETED  RUNNING --> FAILED  RUNNING --> TIMED_OUT  FAILED --> RETRYING  RETRYING --> RUNNING  FAILED --> DLQ  DLQ --> RETRYING   `

10\. Workflow Execution Flow
============================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   sequenceDiagram  participant Trigger  participant RabbitMQ  participant ExecutionEngine  participant Connector  participant Audit  Trigger->>RabbitMQ: Create Execution Event  RabbitMQ->>ExecutionEngine: Consume Event  ExecutionEngine->>ExecutionEngine: Create Execution  ExecutionEngine->>Connector: Execute Node  Connector-->>ExecutionEngine: Result  ExecutionEngine->>Audit: Audit Event  ExecutionEngine-->>ExecutionEngine: Update State   `

11\. Scheduler Trigger Flow
===========================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   sequenceDiagram  participant Scheduler  participant Workflow  participant RabbitMQ  Scheduler->>Workflow: Find Active Workflows  Workflow-->>Scheduler: Eligible Workflows  Scheduler->>RabbitMQ: Publish Execution Request   `

12\. Webhook Trigger Flow
=========================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   sequenceDiagram  participant Client  participant API  participant RabbitMQ  Client->>API: Webhook Request  API->>RabbitMQ: Execution Request  RabbitMQ-->>API: Accepted  API-->>Client: 202 Accepted   `

13\. Connector Execution Flow
=============================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   flowchart TD  START  VALIDATE  EXECUTE  SUCCESS  FAILURE  START --> VALIDATE  VALIDATE --> EXECUTE  EXECUTE --> SUCCESS  EXECUTE --> FAILURE   `

14\. Error Handling Strategy
============================

Principles
----------

*   Fail fast
    
*   Fail visibly
    
*   Never swallow exceptions
    

Layers
------

### Controller

Returns standardized error responses.

### Service

Throws business exceptions.

### Connector

Wraps integration exceptions.

### Execution Engine

Captures failures and updates execution state.

15\. Logging Strategy
=====================

Logging Format
--------------

JSON Structured Logging

Required Fields
---------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   timestamp  correlationId  executionId  workflowId  module  level  message   `

Security Rules
--------------

Never log:

*   Passwords
    
*   JWT tokens
    
*   Secrets
    
*   Authorization headers
    

16\. Monitoring Strategy
========================

Metrics
-------

Track:

*   Execution Count
    
*   Success Rate
    
*   Failure Rate
    
*   Retry Count
    
*   Queue Depth
    
*   API Requests
    

Health Checks
-------------

Track:

*   PostgreSQL
    
*   RabbitMQ
    
*   Redis
    

17\. Audit Strategy
===================

Every significant action generates an audit event.

Examples:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   LOGIN  LOGOUT  WORKFLOW_CREATED  WORKFLOW_PUBLISHED  WORKFLOW_ACTIVATED  EXECUTION_STARTED  EXECUTION_FAILED   `

18\. Caching Strategy
=====================

Redis Usage
-----------

### Cache

Workflow metadata

### Cache

Connector configuration

### Cache

Frequently accessed dashboard data

Not Cached
----------

Execution state

Audit events

Authentication data

19\. Resilience Strategy
========================

Retry
-----

Execution retry policy.

Timeout
-------

Per connector timeout.

DLQ
---

Failed executions moved to DLQ.

Manual Recovery
---------------

User-triggered retry.

20\. Scalability Strategy
=========================

Current:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   Single JVM  Single Database  Single RabbitMQ  Single Redis   `

MVP Deployment.

Future:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   Multiple API instances  RabbitMQ cluster  Redis cluster  PostgreSQL HA   `

No architectural redesign required.

21\. Architecture Decision Records
==================================

Referenced ADRs:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   ADR-001 Modular Monolith  ADR-002 Workflow Storage Strategy  ADR-003 Execution Engine Design  ADR-004 RabbitMQ Messaging Strategy  ADR-005 Workflow Versioning Strategy   `

These ADRs will be generated separately.

22\. Architecture Risks
=======================

Risk
----

Module coupling.

### Mitigation

Architecture guardrails.

Risk
----

Workflow execution complexity.

### Mitigation

Explicit state machine.

Risk
----

AI-generated architectural drift.

### Mitigation

Agent governance documents.

23\. Requirements Traceability
==============================

ModuleRequirementsauth-moduleAUTH-\*user-moduleUSER-\*workflow-moduleWF-\*connector-moduleTRG-_, ACT-_, CON-\*execution-moduleEXEC-\*monitoring-moduleMON-\*audit-moduleAUD-\*shared-moduleINF-\*

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML
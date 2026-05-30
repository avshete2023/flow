Database Design
===============

Version: 1.0

Database Engine: PostgreSQL

Architecture Style: Modular Monolith

1\. Database Design Principles
==============================

DBP-001
-------

Every table has a clear owning module.

DBP-002
-------

Every business entity uses UUID primary keys.

DBP-003
-------

Workflow execution must always reference workflow versions.

DBP-004
-------

Auditability is mandatory.

DBP-005
-------

Soft delete preferred over hard delete for business entities.

DBP-006
-------

Workflow definitions stored as versioned JSON documents.

2\. Schema Ownership
====================

ModuleTablesuser-moduleusersauth-modulerefresh\_tokensworkflow-moduleworkflows, workflow\_versionsexecution-moduleworkflow\_executions, execution\_stepsaudit-moduleaudit\_eventsconnector-moduleconnector\_configsmonitoring-moduleaggregated views onlyshared-modulenone

3\. High-Level ER Diagram
=========================

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   erDiagram  USERS ||--o{ WORKFLOWS : owns  WORKFLOWS ||--o{ WORKFLOW_VERSIONS : contains  WORKFLOW_VERSIONS ||--o{ WORKFLOW_EXECUTIONS : executes  WORKFLOW_EXECUTIONS ||--o{ EXECUTION_STEPS : contains  USERS ||--o{ AUDIT_EVENTS : creates  WORKFLOWS ||--o{ AUDIT_EVENTS : generates  WORKFLOW_EXECUTIONS ||--o{ AUDIT_EVENTS : generates  USERS ||--o{ REFRESH_TOKENS : owns   `

4\. Common Audit Columns
========================

All business tables include:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   created_at  created_by  updated_at  updated_by  deleted_at  deleted_by   `

Soft delete supported via:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   deleted_at   `

NULL = active

NOT NULL = deleted

5\. User Module
===============

users
-----

### Purpose

Platform users.

### Table

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   users   `

### Columns

ColumnTypeidUUID PKemailVARCHAR(255)password\_hashVARCHAR(255)roleVARCHAR(50)first\_nameVARCHAR(100)last\_nameVARCHAR(100)is\_activeBOOLEANcreated\_atTIMESTAMPupdated\_atTIMESTAMPdeleted\_atTIMESTAMP

### Constraints

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   UNIQUE(email)   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_users_email  idx_users_role   `

6\. Auth Module
===============

refresh\_tokens
---------------

### Purpose

Manage refresh tokens.

### Columns

ColumnTypeidUUID PKuser\_idUUID FKtokenVARCHAR(512)expires\_atTIMESTAMPrevokedBOOLEANcreated\_atTIMESTAMP

### Relationships

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   refresh_tokens.user_id  → users.id   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_refresh_user  idx_refresh_token   `

7\. Workflow Module
===================

workflows
---------

### Purpose

Workflow metadata.

### Columns

ColumnTypeidUUID PKowner\_idUUID FKnameVARCHAR(255)descriptionTEXTstatusVARCHAR(50)active\_version\_idUUIDcreated\_atTIMESTAMPupdated\_atTIMESTAMPdeleted\_atTIMESTAMP

### Status Values

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   DRAFT  PUBLISHED  ACTIVE  INACTIVE  ARCHIVED   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_workflow_owner  idx_workflow_status   `

workflow\_versions
------------------

### Purpose

Immutable workflow definitions.

### Columns

ColumnTypeidUUID PKworkflow\_idUUID FKversion\_numberINTEGERdefinition\_jsonJSONBpublished\_atTIMESTAMPcreated\_atTIMESTAMP

### Example JSON Structure

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   {    "trigger": {},    "nodes": [],    "edges": []  }   `

### Constraints

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   UNIQUE(workflow_id, version_number)   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_workflow_version  GIN(definition_json)   `

8\. Connector Module
====================

connector\_configs
------------------

### Purpose

Connector-specific configuration.

### Columns

ColumnTypeidUUID PKworkflow\_version\_idUUID FKconnector\_typeVARCHAR(100)configuration\_jsonJSONBcreated\_atTIMESTAMP

### Examples

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   HTTP  DELAY  CONDITIONAL  LOG  WEBHOOK  SCHEDULER   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_connector_type  GIN(configuration_json)   `

9\. Execution Module
====================

workflow\_executions
--------------------

### Purpose

Workflow run tracking.

### Columns

ColumnTypeidUUID PKworkflow\_version\_idUUID FKstatusVARCHAR(50)correlation\_idVARCHAR(100)started\_atTIMESTAMPcompleted\_atTIMESTAMPretry\_countINTEGERerror\_messageTEXTexecution\_contextJSONB

### Status Values

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   PENDING  QUEUED  RUNNING  COMPLETED  FAILED  TIMED_OUT  DLQ   `

### Relationships

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   workflow_version_id  → workflow_versions.id   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_execution_status  idx_execution_workflow  idx_execution_started  idx_execution_correlation   `

execution\_steps
----------------

### Purpose

Node-level execution tracking.

### Columns

ColumnTypeidUUID PKexecution\_idUUID FKnode\_idVARCHAR(255)connector\_typeVARCHAR(100)statusVARCHAR(50)started\_atTIMESTAMPcompleted\_atTIMESTAMPinput\_payloadJSONBoutput\_payloadJSONBerror\_messageTEXT

### Relationships

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   execution_id  → workflow_executions.id   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_step_execution  idx_step_status   `

10\. Audit Module
=================

audit\_events
-------------

### Purpose

Store auditable platform activity.

### Columns

ColumnTypeidUUID PKactor\_idUUID FKevent\_typeVARCHAR(100)resource\_typeVARCHAR(100)resource\_idUUIDdetails\_jsonJSONBoccurred\_atTIMESTAMP

### Event Types

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   LOGIN  LOGOUT  USER_REGISTERED  WORKFLOW_CREATED  WORKFLOW_UPDATED  WORKFLOW_PUBLISHED  WORKFLOW_ACTIVATED  EXECUTION_STARTED  EXECUTION_COMPLETED  EXECUTION_FAILED   `

### Indexes

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   idx_audit_actor  idx_audit_event  idx_audit_resource  idx_audit_occurred   `

11\. Database Relationships
===========================

User Ownership
--------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   erDiagram  USERS ||--o{ WORKFLOWS : owns   `

Workflow Versioning
-------------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   erDiagram  WORKFLOWS ||--o{ WORKFLOW_VERSIONS : contains   `

Execution Tracking
------------------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   erDiagram  WORKFLOW_VERSIONS ||--o{ WORKFLOW_EXECUTIONS : executes  WORKFLOW_EXECUTIONS ||--o{ EXECUTION_STEPS : contains   `

Auditing
--------

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   erDiagram  USERS ||--o{ AUDIT_EVENTS : generates   `

12\. Workflow Storage Strategy
==============================

Decision
--------

Store workflow graphs as JSONB.

Rationale
---------

### Benefits

*   Flexible graph structure
    
*   Easy versioning
    
*   No schema changes for new nodes
    

### Risks

*   Complex querying
    

### Mitigation

*   Store operational data relationally
    
*   Use JSON Schema validation
    

13\. Soft Delete Strategy
=========================

Applied To:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   users  workflows   `

Not Applied To:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   workflow_versions  workflow_executions  execution_steps  audit_events   `

Reason:

Historical records must remain immutable.

14\. Data Retention Strategy
============================

Workflow Definitions
--------------------

Retain forever.

Workflow Versions
-----------------

Retain forever.

Audit Events
------------

Default retention:

1 year

Configurable.

Execution Records
-----------------

Default retention:

90 days

Configurable.

Refresh Tokens
--------------

Remove after expiration.

15\. Partitioning Strategy
==========================

Current MVP
-----------

No partitioning.

Future
------

Partition:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   workflow_executions  audit_events   `

By month.

16\. Database Security
======================

Password Storage
----------------

BCrypt hash only.

Token Storage
-------------

Never store JWT access tokens.

Store refresh tokens only.

Sensitive Data
--------------

Mask before persistence:

*   Passwords
    
*   Authorization headers
    
*   Secrets
    

17\. Database Performance Strategy
==================================

Read Optimization
-----------------

Indexes on:

*   email
    
*   workflow status
    
*   execution status
    
*   audit timestamp
    

JSON Optimization
-----------------

Use:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   GIN   `

Indexes on:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   definition_json  configuration_json   `

18\. Migration Strategy
=======================

Tool:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   Flyway   `

Rules:

*   No manual schema changes.
    
*   All changes versioned.
    
*   Roll-forward only.
    

19\. Requirements Traceability
==============================

EntityRequirementusersAUTH-_, USER-_refresh\_tokensAUTH-\*workflowsWF-\*workflow\_versionsWF-\*connector\_configsCON-\*workflow\_executionsEXEC-\*execution\_stepsEXEC-\*audit\_eventsAUD-\*

20\. Database Risks
===================

Risk
----

Large execution table growth.

### Mitigation

Retention policies.

Risk
----

Large workflow JSON documents.

### Mitigation

JSON schema validation.

Risk
----

Audit growth.

### Mitigation

Retention + future partitioning.
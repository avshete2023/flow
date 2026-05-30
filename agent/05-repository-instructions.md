Repository Instructions
=======================

Repository Purpose
------------------

This repository implements the Flow workflow orchestration platform.

Approved Technology Stack
=========================

Backend:

*   Java 21
    
*   Spring Boot 3.x
    
*   PostgreSQL
    
*   Redis
    
*   RabbitMQ
    
*   Flyway
    
*   MapStruct
    

Frontend:

*   React
    
*   TypeScript
    

Module Ownership
================

auth-module
-----------

Owns:

*   Authentication
    
*   JWT
    
*   Refresh Tokens
    

user-module
-----------

Owns:

*   Users
    
*   Profiles
    
*   Roles
    

workflow-module
---------------

Owns:

*   Workflows
    
*   Workflow Versions
    
*   Workflow Validation
    

execution-module
----------------

Owns:

*   Executions
    
*   Retry Logic
    
*   DLQ Logic
    
*   State Machine
    

connector-module
----------------

Owns:

*   Connector SPI
    
*   Trigger SPI
    
*   Connector Registry
    

monitoring-module
-----------------

Owns:

*   Metrics
    
*   Dashboard APIs
    
*   Health Checks
    

audit-module
------------

Owns:

*   Audit Events
    
*   Audit Queries
    

shared-module
-------------

Owns:

*   Shared utilities
    
*   Exceptions
    
*   Constants
    

Must not contain business logic.

Repository Rules
================

Never:

*   Create new top-level modules
    
*   Bypass module ownership
    
*   Share repositories between modules
    

Branch Naming
=============

feature/AUTH-001

feature/WF-003

feature/EXEC-009

Commit Format
=============

feat(auth): implement AUTH-001 registration

fix(workflow): workflow validation bug

test(execution): add retry integration tests

Pull Request Requirements
=========================

Every PR must include:

*   Story ID
    
*   Acceptance Criteria
    
*   Tests Added
    
*   Documentation Updates
    
*   Known Limitations
    

Architecture Authority
======================

If implementation questions arise, consult:

1.  System Architecture
    
2.  Database Design
    
3.  API Specification
    
4.  Security Design
    
5.  AI Agent Implementation Guide
    

These documents are authoritative.
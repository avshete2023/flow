GitHub Copilot Instructions
===========================

Project Overview
----------------

This repository implements Flow, a workflow orchestration platform built for learning enterprise architecture and workflow engine design.

Primary stack:

*   Java 21
    
*   Spring Boot 3.x
    
*   PostgreSQL
    
*   RabbitMQ
    
*   Redis
    
*   React
    
*   TypeScript
    

Mandatory Reading Order
=======================

Before implementing any story:

1.  docs/03-system-architecture.md
    
2.  docs/04-database-design.md
    
3.  docs/05-api-specification.md
    
4.  docs/06-security-design.md
    
5.  docs/07-project-structure.md
    
6.  docs/09-ai-agent-implementation-guide.md
    
7.  agent/01-agent-rules.md
    
8.  agent/04-definition-of-done.md
    

Architecture Rules
==================

*   Respect module ownership
    
*   No cross-module repository access
    
*   No business logic in controllers
    
*   DTOs only across API boundaries
    
*   Shared module contains no business logic
    

Security Rules
==============

*   JWT authentication required
    
*   Authorization checks required
    
*   Never expose secrets
    
*   Never log sensitive information
    

Database Rules
==============

*   Flyway migrations only
    
*   UUID primary keys
    
*   Immutable workflow versions
    
*   Immutable execution records
    

API Rules
=========

*   Follow docs/05-api-specification.md exactly
    
*   Use request and response DTOs
    
*   Validate all inputs
    
*   Maintain API versioning
    

Testing Rules
=============

Every story requires:

*   Unit tests
    
*   Integration tests where applicable
    

Bug fixes require regression tests.

Implementation Protocol
=======================

For each story:

1.  Read story requirements
    
2.  Verify module ownership
    
3.  Implement DTOs
    
4.  Implement domain model
    
5.  Implement service layer
    
6.  Implement API layer
    
7.  Implement tests
    
8.  Validate Definition of Done

9.  Run full verification build:
    `mvn clean install`
    (or `./mvnw clean install`) and ensure it passes.
    

Forbidden Actions
=================

*   Introduce new frameworks
    
*   Create new top-level modules
    
*   Bypass security
    
*   Expose entities
    
*   Ignore acceptance criteria
    
*   Modify architecture without approval
    

Expected Output
===============

When implementing a story, provide:

*   Files created
    
*   Files modified
    
*   Acceptance criteria covered
    
*   Tests added
    
*   Known limitations
    

Follow the architecture documents as the source of truth.
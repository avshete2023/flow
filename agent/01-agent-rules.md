Agent Rules
===========

Version: 1.0

Purpose
-------

These rules govern all AI-assisted development activities.

These rules are mandatory.

If a story conflicts with these rules, stop and request clarification.

Architecture First
==================

Before implementing any story, read:

1.  docs/03-system-architecture.md
    
2.  docs/04-database-design.md
    
3.  docs/05-api-specification.md
    
4.  docs/06-security-design.md
    
5.  docs/07-project-structure.md
    
6.  docs/09-ai-agent-implementation-guide.md
    

Mandatory Rules
===============

Rule 1
------

Respect module ownership.

Never implement functionality in the wrong module.

Rule 2
------

Never access repositories across modules.

Use services or events.

Rule 3
------

Never expose JPA entities through APIs.

Use DTOs only.

Rule 4
------

Never place business logic inside controllers.

Rule 5
------

Every endpoint requires validation.

Rule 6
------

Every story requires tests.

Rule 7
------

Never hardcode secrets.

Rule 8
------

Never introduce new frameworks without approval.

Rule 9
------

Never modify database schema outside Flyway migrations.

Rule 10
-------

Never bypass security requirements.

Implementation Order
====================

Always implement:

DTOs→ Validation→ Entity→ Repository→ Service→ Controller→ Tests

Forbidden Actions
=================

*   Creating undocumented features
    
*   Creating additional modules
    
*   Disabling tests
    
*   Removing validation
    
*   Removing authentication
    
*   Returning entities directly
    
*   Ignoring acceptance criteria
    

Completion Requirements
=======================

Before marking a story complete:

*   Build passes
    
*   Tests pass
    
*   Acceptance criteria satisfied
    
*   Documentation updated
    
*   Architecture rules respected
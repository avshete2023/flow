Coding Standards
================

Version: 1.0

Java Standards
--------------

Language:

Java 21

Framework:

Spring Boot 3.x

Build:

Maven

Dependency Injection
====================

Use:

Constructor Injection Only

Example:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @RequiredArgsConstructor  @Service  public class WorkflowService {      private final WorkflowRepository repository;  }   `

Never use:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   @Autowired   `

DTO Standards
=============

Requests:

CreateWorkflowRequest

Responses:

WorkflowResponse

Never use:

WorkflowDTO

Entity Standards
================

Entities:

*   Singular names
    
*   UUID primary keys
    
*   Audit fields
    

Entities must not contain business logic.

Controller Standards
====================

Controllers may:

*   Validate requests
    
*   Call services
    
*   Return DTOs
    

Controllers may not:

*   Access repositories
    
*   Perform transactions
    
*   Implement business rules
    

Service Standards
=================

Services contain business logic.

Transactions belong here.

Repository Standards
====================

Repositories perform persistence only.

Mapping Standards
=================

Use MapStruct.

Avoid manual mapping unless justified.

Exception Standards
===================

Use custom exceptions.

Handle exceptions globally.

Logging Standards
=================

Use SLF4J.

Never log:

*   Passwords
    
*   JWTs
    
*   Secrets
    
*   Authorization headers
    

Testing Standards
=================

Every story requires:

*   Unit tests
    
*   Integration tests where applicable
    

Coverage target:

70% minimum

API Standards
=============

All APIs:

*   Versioned
    
*   Documented
    
*   Validated
    

Base URL:

/api/v1

Database Standards
==================

Use Flyway.

Never modify schema manually.

Use UUID primary keys.

Published workflow versions are immutable.

Execution records are immutable.
package com.flow.auth.service;

import com.flow.auth.event.AuthenticationAuditEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes authentication audit events.
 */
@Service
public class AuthenticationAuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AuthenticationAuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishLoginSuccessEvent(String email, java.util.UUID userId) {
        applicationEventPublisher.publishEvent(new AuthenticationAuditEvent(
                userId,
                "LOGIN_SUCCESS",
                "{\"email\":\"" + email + "\"}",
                java.time.LocalDateTime.now()
        ));
    }

    public void publishLoginFailureEvent(String email) {
        applicationEventPublisher.publishEvent(new AuthenticationAuditEvent(
                null,
                "LOGIN_FAILURE",
                "{\"email\":\"" + email + "\"}",
                java.time.LocalDateTime.now()
        ));
    }

    public void publishLogoutEvent(java.util.UUID userId) {
        applicationEventPublisher.publishEvent(new AuthenticationAuditEvent(
                userId,
                "LOGOUT",
                "{}",
                java.time.LocalDateTime.now()
        ));
    }

    public void publishUnauthorizedAccessEvent(String details) {
        applicationEventPublisher.publishEvent(new AuthenticationAuditEvent(
                null,
                "UNAUTHORIZED_ACCESS",
                "{\"reason\":\"" + details + "\"}",
                java.time.LocalDateTime.now()
        ));
    }
}


package com.flow.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.flow.auth.event.AuthenticationAuditEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class AuthenticationAuditEventPublisherTest {

    @Test
    void shouldPublishLoginSuccessEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        AuthenticationAuditEventPublisher auditPublisher = new AuthenticationAuditEventPublisher(publisher);

        UUID userId = UUID.randomUUID();
        auditPublisher.publishLoginSuccessEvent("user@example.com", userId);

        verify(publisher).publishEvent(any(AuthenticationAuditEvent.class));
    }

    @Test
    void shouldPublishLoginFailureEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        AuthenticationAuditEventPublisher auditPublisher = new AuthenticationAuditEventPublisher(publisher);

        auditPublisher.publishLoginFailureEvent("user@example.com");

        verify(publisher).publishEvent(any(AuthenticationAuditEvent.class));
    }

    @Test
    void shouldPublishLogoutEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        AuthenticationAuditEventPublisher auditPublisher = new AuthenticationAuditEventPublisher(publisher);

        UUID userId = UUID.randomUUID();
        auditPublisher.publishLogoutEvent(userId);

        verify(publisher).publishEvent(any(AuthenticationAuditEvent.class));
    }

    @Test
    void shouldPublishUnauthorizedAccessEvent() {
        ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
        AuthenticationAuditEventPublisher auditPublisher = new AuthenticationAuditEventPublisher(publisher);

        auditPublisher.publishUnauthorizedAccessEvent("Expired token");

        verify(publisher).publishEvent(any(AuthenticationAuditEvent.class));
    }
}


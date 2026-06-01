package com.flow.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.flow.auth.event.AuthenticationAuditEvent;
import com.flow.audit.service.AuditService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthenticationAuditEventListenerTest {

    @Test
    void shouldListenToAndRecordAuthenticationAuditEvents() {
        AuditService auditService = Mockito.mock(AuditService.class);
        AuthenticationAuditEventListener listener = new AuthenticationAuditEventListener(Optional.of(auditService));

        UUID userId = UUID.randomUUID();
        AuthenticationAuditEvent event = new AuthenticationAuditEvent(
                userId,
                "LOGIN_SUCCESS",
                "{\"email\":\"user@example.com\"}",
                LocalDateTime.now()
        );

        listener.onAuthenticationAuditEvent(event);

        verify(auditService).recordSecurityEvent(eq(userId), eq("LOGIN_SUCCESS"), any());
    }
}



package com.flow.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.auth.config.JwtProperties;
import com.flow.auth.dto.LoginRequest;
import com.flow.auth.dto.LoginResponse;
import com.flow.auth.exception.InvalidCredentialsException;
import com.flow.auth.security.JwtPrincipal;
import com.flow.auth.security.JwtTokenGenerator;
import com.flow.user.domain.entity.User;
import com.flow.user.domain.model.UserRole;
import com.flow.user.service.UserAccountPersistenceService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";
    private static ValidatorFactory validatorFactory;

    @Mock
    private UserAccountPersistenceService userAccountPersistenceService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenGenerator jwtTokenGenerator;

    private AuthenticationService authenticationService;

    @BeforeAll
    static void initValidatorFactory() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeValidatorFactory() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(JWT_SECRET);
        jwtProperties.setIssuer("flow-api");
        jwtProperties.setAccessTokenTtl(Duration.ofMinutes(60));
        Validator beanValidator = validatorFactory.getValidator();
        authenticationService = new AuthenticationService(
                userAccountPersistenceService,
                passwordEncoder,
                jwtTokenGenerator,
                jwtProperties,
                beanValidator
        );
    }

    @Test
    void shouldLoginAndReturnTokensForValidCredentials() {
        User user = new User(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "john@example.com",
                "hashed-password",
                UserRole.USER,
                "John",
                "Doe",
                true,
                null,
                null,
                null,
                null
        );
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");

        when(userAccountPersistenceService.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hashed-password")).thenReturn(true);
        when(jwtTokenGenerator.generateAccessToken(new JwtPrincipal(user.getId(), user.getEmail(), user.getRole())))
                .thenReturn("access-token");

        LoginResponse response = authenticationService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.expiresIn()).isEqualTo(3600);
        verify(jwtTokenGenerator).generateAccessToken(new JwtPrincipal(user.getId(), user.getEmail(), user.getRole()));
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = new User(
                UUID.randomUUID(),
                "john@example.com",
                "hashed-password",
                UserRole.USER,
                "John",
                "Doe",
                true,
                null,
                null,
                null,
                null
        );
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword@123");

        when(userAccountPersistenceService.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword@123", "hashed-password")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request));

        assertThat(exception.getMessage()).isEqualTo("Invalid email or password");
        verify(jwtTokenGenerator, never()).generateAccessToken(any());
    }

    @Test
    void shouldRejectInactiveUser() {
        User user = new User(
                UUID.randomUUID(),
                "john@example.com",
                "hashed-password",
                UserRole.USER,
                "John",
                "Doe",
                false,
                null,
                null,
                null,
                null
        );
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");

        when(userAccountPersistenceService.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenGenerator, never()).generateAccessToken(any());
    }

    @Test
    void shouldRejectInvalidRequestBeforeLookup() {
        LoginRequest request = new LoginRequest("not-an-email", "short");

        assertThrows(ConstraintViolationException.class, () -> authenticationService.login(request));
        verify(userAccountPersistenceService, never()).findByEmail(any());
    }
}


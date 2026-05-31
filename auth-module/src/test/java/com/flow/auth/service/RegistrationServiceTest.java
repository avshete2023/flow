package com.flow.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.auth.dto.RegisterUserRequest;
import com.flow.auth.dto.RegisterUserResponse;
import com.flow.auth.validator.RegistrationValidator;
import com.flow.user.domain.entity.User;
import com.flow.user.domain.model.UserRole;
import com.flow.user.service.UserAccountPersistenceService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserAccountPersistenceService userAccountPersistenceService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static ValidatorFactory validatorFactory;

    private RegistrationService registrationService;

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
        RegistrationValidator registrationValidator = new RegistrationValidator();
        Validator beanValidator = validatorFactory.getValidator();
        registrationService = new RegistrationService(
                userAccountPersistenceService,
                passwordEncoder,
                registrationValidator,
                beanValidator
        );
    }

    @Test
    void shouldRegisterUserWhenRequestIsValid() {
        RegisterUserRequest request = new RegisterUserRequest(
                "john@example.com",
                "Password@123",
                "John",
                "Doe"
        );
        UUID userId = UUID.randomUUID();

        when(userAccountPersistenceService.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(userAccountPersistenceService.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0, User.class);
            return new User(
                    userId,
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getRole(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.isActive(),
                    user.getCreatedBy(),
                    user.getUpdatedBy(),
                    user.getDeletedAt(),
                    user.getDeletedBy()
            );
        });

        RegisterUserResponse response = registrationService.register(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.message()).isEqualTo("Registration successful");
        verify(passwordEncoder).encode("Password@123");

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userAccountPersistenceService).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUserCaptor.getValue().getPasswordHash()).isNotEqualTo("Password@123");
        assertThat(savedUserCaptor.getValue().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
                "duplicate@example.com",
                "Password@123",
                "Jane",
                "Doe"
        );

        when(userAccountPersistenceService.existsByEmail("duplicate@example.com")).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> registrationService.register(request));

        assertThat(exception.getMessage()).isEqualTo("Email already registered");
        verify(userAccountPersistenceService, never()).save(any(User.class));
    }

    @Test
    void shouldRejectInvalidEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
                "not-an-email",
                "Password@123",
                "John",
                "Doe"
        );

        assertThrows(ConstraintViolationException.class, () -> registrationService.register(request));
        verify(userAccountPersistenceService, never()).existsByEmail(any());
    }

    @Test
    void shouldRejectWeakPassword() {
        RegisterUserRequest request = new RegisterUserRequest(
                "john@example.com",
                "weakpass",
                "John",
                "Doe"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> registrationService.register(request));

        assertThat(exception.getMessage()).isEqualTo("Password does not meet strength requirements");
        verify(passwordEncoder, never()).encode(any());
        verify(userAccountPersistenceService, never()).save(any(User.class));
    }
}



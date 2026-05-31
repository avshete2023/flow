package com.flow.auth.service;

import com.flow.auth.dto.RegisterUserRequest;
import com.flow.auth.dto.RegisterUserResponse;
import com.flow.auth.validator.RegistrationValidator;
import com.flow.user.domain.entity.User;
import com.flow.user.domain.model.UserRole;
import com.flow.user.service.UserAccountPersistenceService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserAccountPersistenceService userAccountPersistenceService;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationValidator registrationValidator;
    private final Validator beanValidator;

    public RegistrationService(
            UserAccountPersistenceService userAccountPersistenceService,
            PasswordEncoder passwordEncoder,
            RegistrationValidator registrationValidator,
            Validator beanValidator
    ) {
        this.userAccountPersistenceService = userAccountPersistenceService;
        this.passwordEncoder = passwordEncoder;
        this.registrationValidator = registrationValidator;
        this.beanValidator = beanValidator;
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        Set<ConstraintViolation<RegisterUserRequest>> violations = beanValidator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        registrationValidator.validatePasswordStrength(request.password());

        if (userAccountPersistenceService.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(
                null,
                request.email(),
                encodedPassword,
                UserRole.USER,
                request.firstName(),
                request.lastName(),
                true,
                null,
                null,
                null,
                null
        );

        User savedUser = userAccountPersistenceService.save(user);

        return new RegisterUserResponse(savedUser.getId(), savedUser.getEmail(), "Registration successful");
    }
}

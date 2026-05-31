package com.flow.auth.service;

import com.flow.auth.config.JwtProperties;
import com.flow.auth.dto.LoginRequest;
import com.flow.auth.dto.LoginResponse;
import com.flow.auth.exception.InvalidCredentialsException;
import com.flow.auth.security.JwtPrincipal;
import com.flow.auth.security.JwtTokenGenerator;
import com.flow.user.domain.entity.User;
import com.flow.user.service.UserAccountPersistenceService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserAccountPersistenceService userAccountPersistenceService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtProperties jwtProperties;
    private final Validator beanValidator;

    public AuthenticationService(
            UserAccountPersistenceService userAccountPersistenceService,
            PasswordEncoder passwordEncoder,
            JwtTokenGenerator jwtTokenGenerator,
            JwtProperties jwtProperties,
            Validator beanValidator
    ) {
        this.userAccountPersistenceService = userAccountPersistenceService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtProperties = jwtProperties;
        this.beanValidator = beanValidator;
    }

    public LoginResponse login(LoginRequest request) {
        Set<ConstraintViolation<LoginRequest>> violations = beanValidator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        User user = findActiveUser(request.email())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        String accessToken = jwtTokenGenerator.generateAccessToken(
                new JwtPrincipal(user.getId(), user.getEmail(), user.getRole())
        );
        String refreshToken = UUID.randomUUID().toString();

        return new LoginResponse(accessToken, refreshToken, jwtProperties.getAccessTokenTtl().toSeconds());
    }

    private Optional<User> findActiveUser(String email) {
        return userAccountPersistenceService.findByEmail(email)
                .filter(User::isActive)
                .filter(user -> user.getDeletedAt() == null);
    }
}


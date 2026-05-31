package com.flow.auth.security;

import com.flow.auth.config.JwtProperties;
import com.flow.user.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtTokenValidator {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenValidator(SecretKey secretKey, JwtProperties jwtProperties) {
        this.secretKey = Objects.requireNonNull(secretKey, "Secret key must not be null");
        this.jwtProperties = Objects.requireNonNull(jwtProperties, "JWT properties must not be null");
    }

    public JwtPrincipal validateAccessToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token must not be blank");
        }

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        String email = claims.get(CLAIM_EMAIL, String.class);
        String role = claims.get(CLAIM_ROLE, String.class);

        if (subject == null || subject.isBlank() || email == null || email.isBlank() || role == null || role.isBlank()) {
            throw new JwtException("JWT token is missing required claims");
        }

        return new JwtPrincipal(UUID.fromString(subject), email, UserRole.valueOf(role));
    }
}


package com.flow.auth.security;

import com.flow.auth.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import javax.crypto.SecretKey;

public class JwtTokenGenerator {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final Clock clock;
    private final JwtProperties jwtProperties;

    public JwtTokenGenerator(SecretKey secretKey, Clock clock, JwtProperties jwtProperties) {
        this.secretKey = Objects.requireNonNull(secretKey, "Secret key must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
        this.jwtProperties = Objects.requireNonNull(jwtProperties, "JWT properties must not be null");
    }

    public String generateAccessToken(JwtPrincipal principal) {
        Objects.requireNonNull(principal, "JWT principal must not be null");
        Objects.requireNonNull(principal.userId(), "User ID must not be null");
        Objects.requireNonNull(principal.email(), "Email must not be null");
        Objects.requireNonNull(principal.role(), "Role must not be null");

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(principal.userId().toString())
                .claim(CLAIM_EMAIL, principal.email())
                .claim(CLAIM_ROLE, principal.role().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}


package com.flow.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flow.auth.config.JwtProperties;
import com.flow.user.domain.model.UserRole;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtTokenValidatorTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";
    private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void shouldValidateAccessTokenAndReturnPrincipal() {
        Instant now = Instant.now();
        SecretKey secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        JwtProperties properties = jwtProperties();
        JwtTokenGenerator generator = new JwtTokenGenerator(secretKey, Clock.fixed(now, ZoneOffset.UTC), properties);
        JwtTokenValidator validator = new JwtTokenValidator(secretKey, properties);

        String token = generator.generateAccessToken(new JwtPrincipal(USER_ID, "john@example.com", UserRole.USER));
        JwtPrincipal principal = validator.validateAccessToken(token);

        assertThat(principal.userId()).isEqualTo(USER_ID);
        assertThat(principal.email()).isEqualTo("john@example.com");
        assertThat(principal.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey() {
        Instant now = Instant.now();
        SecretKey signingKey = Keys.hmacShaKeyFor("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(StandardCharsets.UTF_8));
        SecretKey validationKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        JwtProperties properties = jwtProperties();
        JwtTokenGenerator generator = new JwtTokenGenerator(signingKey, Clock.fixed(now, ZoneOffset.UTC), properties);
        JwtTokenValidator validator = new JwtTokenValidator(validationKey, properties);

        String token = generator.generateAccessToken(new JwtPrincipal(USER_ID, "john@example.com", UserRole.USER));

        assertThrows(JwtException.class, () -> validator.validateAccessToken(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        SecretKey secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        JwtProperties properties = jwtProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(1));
        JwtTokenGenerator generator = new JwtTokenGenerator(secretKey, Clock.fixed(Instant.parse("2000-01-01T00:00:00Z"), ZoneOffset.UTC), properties);
        JwtTokenValidator validator = new JwtTokenValidator(secretKey, properties);

        String token = generator.generateAccessToken(new JwtPrincipal(USER_ID, "john@example.com", UserRole.USER));

        assertThrows(JwtException.class, () -> validator.validateAccessToken(token));
    }

    private static JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(JWT_SECRET);
        properties.setIssuer("flow-api");
        properties.setAccessTokenTtl(Duration.ofMinutes(60));
        return properties;
    }
}



package com.flow.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.auth.config.JwtProperties;
import com.flow.user.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtTokenGeneratorTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";
    private static final Instant FIXED_INSTANT = Instant.parse("2026-05-31T10:15:30Z");
    private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void shouldGenerateSignedAccessTokenWithExpectedClaims() {
        SecretKey secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        JwtProperties properties = jwtProperties();
        JwtTokenGenerator generator = new JwtTokenGenerator(secretKey, Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC), properties);
        JwtPrincipal principal = new JwtPrincipal(USER_ID, "john@example.com", UserRole.USER);

        String token = generator.generateAccessToken(principal);
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer("flow-api")
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("john@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo(UserRole.USER.name());
        assertThat(claims.getIssuer()).isEqualTo("flow-api");
        assertThat(claims.getIssuedAt()).isEqualTo(java.util.Date.from(FIXED_INSTANT));
        assertThat(claims.getExpiration()).isEqualTo(java.util.Date.from(FIXED_INSTANT.plus(Duration.ofMinutes(60))));
    }

    private static JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(JWT_SECRET);
        properties.setIssuer("flow-api");
        properties.setAccessTokenTtl(Duration.ofMinutes(60));
        return properties;
    }
}


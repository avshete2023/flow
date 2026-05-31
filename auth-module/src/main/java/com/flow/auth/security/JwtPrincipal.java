package com.flow.auth.security;

import com.flow.user.domain.model.UserRole;
import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        String email,
        UserRole role
) {
}


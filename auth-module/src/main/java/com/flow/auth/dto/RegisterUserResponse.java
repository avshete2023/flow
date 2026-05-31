package com.flow.auth.dto;

import java.util.UUID;

public record RegisterUserResponse(
        UUID userId,
        String email,
        String message
) {
}

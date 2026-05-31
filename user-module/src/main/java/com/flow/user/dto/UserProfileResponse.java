package com.flow.user.dto;

import com.flow.user.domain.model.UserRole;
import java.util.UUID;

/**
 * Response DTO for user profile data.
 * 
 * Returned by GET /api/v1/users/me.
 * Contains user profile information accessible to authenticated users.
 */
public record UserProfileResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {
}


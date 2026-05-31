package com.flow.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile.
 * 
 * Used for PUT /api/v1/users/me.
 * Allows users to update their firstName and lastName.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName
) {
}


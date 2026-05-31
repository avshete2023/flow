package com.flow.user.domain.mapper;

import com.flow.user.domain.entity.User;
import com.flow.user.dto.UserProfileResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting User entity to UserProfileResponse DTO.
 *
 * Responsibilities:
 * - Convert User entity to response DTOs
 * - Ensure no sensitive data is exposed (e.g., password hash)
 */
@Component
public class UserProfileMapper {

    /**
     * Convert a User entity to a UserProfileResponse DTO.
     *
     * @param user The user entity to convert
     * @return UserProfileResponse DTO with public profile information
     */
    public UserProfileResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}


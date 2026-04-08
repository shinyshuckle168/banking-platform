package com.bankapp.auth.api;

import com.bankapp.auth.domain.User;
import com.bankapp.auth.domain.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        Set<UserRole> roles,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRoles(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

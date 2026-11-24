package com.minijira.user.dto;

import com.minijira.user.entity.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}

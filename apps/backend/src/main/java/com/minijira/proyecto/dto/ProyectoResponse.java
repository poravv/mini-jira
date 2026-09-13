package com.minijira.proyecto.dto;

import com.minijira.user.dto.UserResponse;

import java.time.Instant;
import java.util.List;

public record ProyectoResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        List<UserResponse> members
) {
}

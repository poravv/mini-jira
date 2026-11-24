package com.minijira.proyecto.dto;

import jakarta.validation.constraints.NotNull;

public record MemberRequest(
        @NotNull(message = "User id is required")
        Long userId
) {
}

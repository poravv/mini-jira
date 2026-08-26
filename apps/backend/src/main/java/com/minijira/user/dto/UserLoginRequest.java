package com.minijira.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
        @NotBlank @Size(max = 254) String identifier,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}

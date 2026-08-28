package com.minijira.auth.dto;

import com.minijira.user.entity.UserRole;

public record LoginResponse(String accessToken, String tokenType, Long userId, String username, UserRole role) {
}

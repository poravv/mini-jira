package com.minijira.auth.service;

import com.minijira.user.entity.UserRole;

public record AuthenticatedUser(Long id, String username, UserRole role) {
}

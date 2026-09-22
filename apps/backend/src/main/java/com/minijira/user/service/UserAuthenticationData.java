package com.minijira.user.service;

import com.minijira.user.entity.UserRole;

/** Datos internos que el módulo auth necesita para verificar credenciales. */
public record UserAuthenticationData(Long id, String username, String passwordHash, boolean isActive, UserRole role) {
}

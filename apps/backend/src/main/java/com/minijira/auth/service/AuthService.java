package com.minijira.auth.service;

import com.minijira.auth.dto.LoginRequest;
import com.minijira.auth.dto.LoginResponse;
import com.minijira.auth.exception.AuthenticationException;
import com.minijira.user.service.UserAuthenticationData;
import com.minijira.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAuthenticationData user = userService.findAuthenticationData(request.identifier());
        if (user == null || !user.isActive() || !passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new AuthenticationException();
        }
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.id(), user.username(), user.role());
        return new LoginResponse(jwtService.createToken(authenticatedUser), "Bearer", user.id(), user.username(), user.role());
    }
}

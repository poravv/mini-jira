package com.minijira.auth.controller;

import com.minijira.user.dto.AuthResponse;
import com.minijira.user.dto.UserLoginRequest;
import com.minijira.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "JWT authentication")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain a JWT")
    public AuthResponse login(@Valid @RequestBody UserLoginRequest request) {
        return userService.authenticate(request);
    }
}

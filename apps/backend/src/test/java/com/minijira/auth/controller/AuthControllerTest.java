package com.minijira.auth.controller;

import com.minijira.auth.dto.LoginResponse;
import com.minijira.auth.exception.AuthenticationException;
import com.minijira.auth.service.AuthService;
import com.minijira.auth.service.JwtService;
import com.minijira.user.entity.UserRole;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void should_authenticate_at_the_auth_endpoint() throws Exception {
        given(authService.login(any())).willReturn(new LoginResponse("jwt", "Bearer", 1L, "ana", UserRole.USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"identifier\":\"ana@example.com\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void should_return_401_for_invalid_credentials() throws Exception {
        given(authService.login(any())).willThrow(new AuthenticationException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"identifier\":\"ana@example.com\",\"password\":\"secreto123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
}

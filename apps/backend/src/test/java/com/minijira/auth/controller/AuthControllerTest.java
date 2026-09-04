package com.minijira.auth.controller;

import com.minijira.user.dto.AuthResponse;
import com.minijira.user.dto.UserLoginRequest;
import com.minijira.user.dto.UserResponse;
import com.minijira.user.entity.UserRole;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void should_return_jwt_response_when_credentials_are_valid() throws Exception {
        Instant now = Instant.parse("2026-08-25T12:00:00Z");
        UserResponse user = new UserResponse(1L, "ana", "ana@example.com", "Ana", "Admin", true,
                UserRole.USER, now, now);
        given(userService.authenticate(new UserLoginRequest("ana@example.com", "secreto123")))
                .willReturn(new AuthResponse("signed.jwt.token", "Bearer", 3600, user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"identifier\":\"ana@example.com\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed.jwt.token"))
                .andExpect(jsonPath("$.user.username").value("ana"));
    }
}

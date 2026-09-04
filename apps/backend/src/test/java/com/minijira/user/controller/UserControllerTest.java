package com.minijira.user.controller;

import com.minijira.auth.service.JwtService;
import com.minijira.user.dto.UserResponse;
import com.minijira.user.dto.UserStatusRequest;
import com.minijira.user.entity.UserRole;
import com.minijira.user.exception.UserNotFoundException;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void should_list_users_without_exposing_password() throws Exception {
        UserResponse user = user(1L, true);
        given(userService.findAll(null)).willReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("ana"))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void should_validate_password_when_registering() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\",\"email\":\"ana@example.com\",\"password\":\"short\",\"firstName\":\"Ana\",\"lastName\":\"Admin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void should_deactivate_user() throws Exception {
        given(userService.updateStatus(1L, new UserStatusRequest(false))).willReturn(user(1L, false));

        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void should_return_404_for_missing_user() throws Exception {
        willThrow(new UserNotFoundException(99L)).given(userService).findById(99L);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    private static UserResponse user(Long id, boolean isActive) {
        Instant now = Instant.parse("2026-08-25T12:00:00Z");
        return new UserResponse(id, "ana", "ana@example.com", "Ana", "Admin", isActive,
                UserRole.ADMIN, now, now);
    }
}

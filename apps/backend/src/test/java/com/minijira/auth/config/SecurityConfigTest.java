package com.minijira.auth.config;

import com.minijira.auth.service.JwtService;
import com.minijira.issue.controller.IssueController;
import com.minijira.issue.service.IssueService;
import com.minijira.proyecto.controller.ProyectoController;
import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.service.ProyectoService;
import com.minijira.user.controller.UserController;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({IssueController.class, UserController.class, ProyectoController.class})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProyectoService proyectoService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void should_reject_protected_endpoint_without_token() throws Exception {
        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_reject_an_invalid_bearer_token() throws Exception {
        mockMvc.perform(get("/api/issues")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_allow_authenticated_user_to_read_issues() throws Exception {
        given(issueService.findAll(null, null, null, null)).willReturn(List.of());

        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_restrict_user_management_to_admins() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_allow_authenticated_user_to_read_projects() throws Exception {
        given(proyectoService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_restrict_project_membership_management_to_admins() throws Exception {
        mockMvc.perform(post("/api/proyectos/1/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_forbid_project_update_when_caller_is_not_admin() throws Exception {
        mockMvc.perform(put("/api/proyectos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Portal interno\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_forbid_project_deletion_when_caller_is_not_admin() throws Exception {
        mockMvc.perform(delete("/api/proyectos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_allow_project_deletion_when_caller_is_admin() throws Exception {
        mockMvc.perform(delete("/api/proyectos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_allow_project_update_when_caller_is_admin() throws Exception {
        given(proyectoService.update(eq(1L), any()))
                .willReturn(new ProyectoResponse(
                        1L, "Portal interno", null, Instant.now(), Instant.now(), List.of()));

        mockMvc.perform(put("/api/proyectos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Portal interno\"}"))
                .andExpect(status().isOk());
    }
}

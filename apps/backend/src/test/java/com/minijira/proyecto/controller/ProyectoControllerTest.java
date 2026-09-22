package com.minijira.proyecto.controller;

import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.exception.ProyectoMemberConflictException;
import com.minijira.proyecto.exception.ProyectoMemberNotFoundException;
import com.minijira.proyecto.exception.ProyectoNotFoundException;
import com.minijira.proyecto.service.ProyectoService;
import com.minijira.user.dto.UserResponse;
import com.minijira.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProyectoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProyectoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProyectoService proyectoService;

    @Test
    void should_create_project_when_data_is_valid() throws Exception {
        given(proyectoService.create(any())).willReturn(project(1L));

        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Portal interno\",\"description\":\"Proyecto del equipo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Portal interno"))
                .andExpect(jsonPath("$.members").isArray());
    }

    @Test
    void should_add_member_when_user_exists() throws Exception {
        given(proyectoService.addMember(eq(1L), eq(2L))).willReturn(project(1L));

        mockMvc.perform(post("/api/proyectos/1/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void should_return_404_when_project_not_found() throws Exception {
        willThrow(new ProyectoNotFoundException(99L))
                .given(proyectoService).findById(99L);

        mockMvc.perform(get("/api/proyectos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project not found: 99"));
    }

    @Test
    void should_return_400_when_project_name_is_blank() throws Exception {
        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void should_return_409_when_member_is_already_in_the_project() throws Exception {
        willThrow(new ProyectoMemberConflictException(1L, 2L))
                .given(proyectoService).addMember(1L, 2L);

        mockMvc.perform(post("/api/proyectos/1/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User 2 is already a member of project 1"));
    }

    @Test
    void should_return_404_when_removing_a_member_that_is_not_in_the_project() throws Exception {
        willThrow(new ProyectoMemberNotFoundException(1L, 9L))
                .given(proyectoService).removeMember(1L, 9L);

        mockMvc.perform(delete("/api/proyectos/1/miembros/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void should_update_project_when_data_is_valid() throws Exception {
        given(proyectoService.update(eq(1L), any())).willReturn(project(1L));

        mockMvc.perform(put("/api/proyectos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Portal interno\",\"description\":\"Proyecto del equipo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Portal interno"));
    }

    @Test
    void should_return_204_when_deleting_a_project() throws Exception {
        mockMvc.perform(delete("/api/proyectos/1"))
                .andExpect(status().isNoContent());

        verify(proyectoService).deleteById(1L);
    }

    private static ProyectoResponse project(Long id) {
        Instant now = Instant.parse("2026-09-12T12:00:00Z");
        UserResponse member = new UserResponse(2L, "ana", "ana@example.com", "Ana", "Admin", true,
                UserRole.USER, now, now);
        return new ProyectoResponse(id, "Portal interno", "Proyecto del equipo", now, now, List.of(member));
    }
}

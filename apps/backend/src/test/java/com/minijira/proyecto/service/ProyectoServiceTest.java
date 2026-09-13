package com.minijira.proyecto.service;

import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.entity.Proyecto;
import com.minijira.proyecto.exception.ProyectoNotFoundException;
import com.minijira.proyecto.repository.ProyectoRepository;
import com.minijira.user.entity.User;
import com.minijira.user.entity.UserRole;
import com.minijira.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void should_add_member_when_user_exists() {
        Proyecto proyecto = proyecto(1L);
        User user = user(2L);
        given(proyectoRepository.findById(1L)).willReturn(Optional.of(proyecto));
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(proyectoRepository.save(proyecto)).willReturn(proyecto);

        ProyectoResponse response = new ProyectoService(proyectoRepository, userRepository)
                .addMember(1L, 2L);

        assertEquals(1, response.members().size());
        assertEquals(2L, response.members().get(0).id());
        verify(proyectoRepository).save(proyecto);
    }

    @Test
    void should_throw_404_when_project_does_not_exist() {
        given(proyectoRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ProyectoNotFoundException.class, () ->
                new ProyectoService(proyectoRepository, userRepository).findById(99L));
    }

    private static Proyecto proyecto(Long id) {
        Proyecto proyecto = org.mockito.Mockito.mock(Proyecto.class);
        given(proyecto.getId()).willReturn(id);
        given(proyecto.getName()).willReturn("Portal interno");
        given(proyecto.getDescription()).willReturn("Proyecto del equipo");
        given(proyecto.getCreatedAt()).willReturn(Instant.parse("2026-09-12T12:00:00Z"));
        given(proyecto.getUpdatedAt()).willReturn(Instant.parse("2026-09-12T12:00:00Z"));
        given(proyecto.getMembers()).willReturn(new LinkedHashSet<>());
        return proyecto;
    }

    private static User user(Long id) {
        User user = org.mockito.Mockito.mock(User.class);
        Instant now = Instant.parse("2026-09-12T12:00:00Z");
        given(user.getId()).willReturn(id);
        given(user.getUsername()).willReturn("ana");
        given(user.getEmail()).willReturn("ana@example.com");
        given(user.getFirstName()).willReturn("Ana");
        given(user.getLastName()).willReturn("Admin");
        given(user.isActive()).willReturn(true);
        given(user.getRole()).willReturn(UserRole.USER);
        given(user.getCreatedAt()).willReturn(now);
        given(user.getUpdatedAt()).willReturn(now);
        return user;
    }
}

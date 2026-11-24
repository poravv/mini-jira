package com.minijira.proyecto.service;

import com.minijira.issue.repository.IssueRepository;
import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.entity.Proyecto;
import com.minijira.proyecto.exception.ProyectoMemberConflictException;
import com.minijira.proyecto.exception.ProyectoNotFoundException;
import com.minijira.proyecto.repository.ProyectoRepository;
import com.minijira.user.entity.User;
import com.minijira.user.entity.UserRole;
import com.minijira.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    @Test
    void should_add_member_when_user_exists() {
        Proyecto proyecto = proyecto(1L);
        User user = user(2L);
        given(proyectoRepository.findById(1L)).willReturn(Optional.of(proyecto));
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(proyectoRepository.saveAndFlush(proyecto)).willReturn(proyecto);

        ProyectoResponse response = proyectoService.addMember(1L, 2L);

        assertEquals(1, response.members().size());
        assertEquals(2L, response.members().get(0).id());
        verify(proyectoRepository).saveAndFlush(proyecto);
    }

    @Test
    void should_throw_conflict_when_member_is_already_in_the_project() {
        Proyecto proyecto = proyecto(1L);
        User user = user(2L);
        proyecto.getMembers().add(user);
        given(proyectoRepository.findById(1L)).willReturn(Optional.of(proyecto));
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        assertThrows(ProyectoMemberConflictException.class, () -> proyectoService.addMember(1L, 2L));

        verify(proyectoRepository, never()).saveAndFlush(proyecto);
    }

    @Test
    void should_throw_conflict_when_the_database_rejects_a_duplicate_member() {
        Proyecto proyecto = proyecto(1L);
        User user = user(2L);
        given(proyectoRepository.findById(1L)).willReturn(Optional.of(proyecto));
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        willThrow(new DataIntegrityViolationException("pk_proyecto_miembro"))
                .given(proyectoRepository).saveAndFlush(proyecto);

        assertThrows(ProyectoMemberConflictException.class, () -> proyectoService.addMember(1L, 2L));
    }

    @Test
    void should_unassign_member_issues_when_the_member_is_removed() {
        Proyecto proyecto = proyecto(1L);
        User user = user(2L);
        proyecto.getMembers().add(user);
        given(proyectoRepository.findById(1L)).willReturn(Optional.of(proyecto));
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        proyectoService.removeMember(1L, 2L);

        assertEquals(0, proyecto.getMembers().size());
        verify(issueRepository).clearAssignee(1L, 2L);
    }

    @Test
    void should_clear_project_issues_when_the_project_is_deleted() {
        Proyecto proyecto = proyecto(1L);
        given(proyectoRepository.findById(1L)).willReturn(Optional.of(proyecto));

        proyectoService.deleteById(1L);

        verify(issueRepository).clearProjectAndAssignee(1L);
        verify(proyectoRepository).delete(proyecto);
    }

    @Test
    void should_throw_404_when_project_does_not_exist() {
        given(proyectoRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ProyectoNotFoundException.class, () -> proyectoService.findById(99L));
    }

    private static Proyecto proyecto(Long id) {
        Proyecto proyecto = new Proyecto();
        Instant now = Instant.parse("2026-09-12T12:00:00Z");
        ReflectionTestUtils.setField(proyecto, "id", id);
        ReflectionTestUtils.setField(proyecto, "createdAt", now);
        ReflectionTestUtils.setField(proyecto, "updatedAt", now);
        proyecto.setName("Portal interno");
        proyecto.setDescription("Proyecto del equipo");
        return proyecto;
    }

    private static User user(Long id) {
        User user = new User();
        Instant now = Instant.parse("2026-09-12T12:00:00Z");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", now);
        ReflectionTestUtils.setField(user, "updatedAt", now);
        user.setUsername("ana");
        user.setEmail("ana@example.com");
        user.setFirstName("Ana");
        user.setLastName("Admin");
        user.setActive(true);
        user.setRole(UserRole.USER);
        return user;
    }
}

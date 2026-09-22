package com.minijira.issue.service;

import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.Issue;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.issue.repository.IssueRepository;
import com.minijira.issue.dto.IssueRequest;
import com.minijira.issue.exception.IssueAssignmentException;
import com.minijira.proyecto.entity.Proyecto;
import com.minijira.proyecto.repository.ProyectoRepository;
import com.minijira.user.entity.User;
import com.minijira.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueService issueService;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_list_all_issues_when_no_filter_is_given() {
        given(issueRepository.search(null, null, null, null)).willReturn(List.of(issue("Caida del login", IssuePriority.ALTA)));

        List<IssueResponse> issues = issueService.findAll(null, null, null, null);

        assertEquals(1, issues.size());
        assertEquals("Caida del login", issues.get(0).title());
    }

    @Test
    void should_only_return_issues_matching_the_filter() {
        given(issueRepository.search(IssueStatus.PENDIENTE, IssuePriority.ALTA, null, null))
                .willReturn(List.of(issue("Caida del login", IssuePriority.ALTA)));

        List<IssueResponse> issues = issueService.findAll(IssueStatus.PENDIENTE, IssuePriority.ALTA, null, null);

        assertEquals(1, issues.size());
        assertEquals(IssuePriority.ALTA, issues.get(0).priority());
    }

    private static Issue issue(String title, IssuePriority priority) {
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setPriority(priority);
        return issue;
    }

    @Test
    void should_delete_an_existing_issue() {
        Issue issue = new Issue();
        given(issueRepository.findById(1L)).willReturn(Optional.of(issue));

        issueService.deleteById(1L);

        verify(issueRepository).delete(issue);
    }

    @Test
    void should_throw_when_deleting_a_missing_issue() {
        given(issueRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(IssueNotFoundException.class, () -> issueService.deleteById(99L));

        verify(issueRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void should_assign_an_issue_to_a_member_of_its_project() {
        authenticateAs("ana", "ROLE_ADMIN");
        Proyecto project = org.mockito.Mockito.mock(Proyecto.class);
        User assignee = org.mockito.Mockito.mock(User.class);
        given(project.getId()).willReturn(7L);
        given(project.getName()).willReturn("Portal interno");
        given(project.getMembers()).willReturn(Set.of(assignee));
        given(assignee.getId()).willReturn(3L);
        given(userRepository.findById(3L)).willReturn(Optional.of(assignee));
        given(proyectoRepository.findById(7L)).willReturn(Optional.of(project));
        given(issueRepository.save(org.mockito.ArgumentMatchers.any(Issue.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        IssueResponse response = issueService.create(new IssueRequest(
                "Preparar despliegue", "", IssueStatus.PENDIENTE, IssuePriority.MEDIA, 7L, 3L));

        assertEquals(7L, response.projectId());
        assertEquals(3L, response.assignee().id());
    }

    @Test
    void should_reject_an_assignee_who_is_not_a_project_member() {
        Proyecto project = org.mockito.Mockito.mock(Proyecto.class);
        User assignee = org.mockito.Mockito.mock(User.class);
        given(project.getId()).willReturn(7L);
        given(project.getMembers()).willReturn(Set.of());
        given(assignee.getId()).willReturn(3L);
        given(userRepository.findById(3L)).willReturn(Optional.of(assignee));
        given(proyectoRepository.findById(7L)).willReturn(Optional.of(project));

        assertThrows(IssueAssignmentException.class, () -> issueService.create(new IssueRequest(
                "Asignación inválida", null, IssueStatus.PENDIENTE, IssuePriority.MEDIA, 7L, 3L)));

        verify(issueRepository, never()).save(org.mockito.ArgumentMatchers.any(Issue.class));
    }

    @Test
    void should_reject_the_assignment_when_the_caller_is_not_a_project_member() {
        authenticateAs("carlos", "ROLE_USER");
        Proyecto project = org.mockito.Mockito.mock(Proyecto.class);
        User assignee = org.mockito.Mockito.mock(User.class);
        given(project.getId()).willReturn(7L);
        given(project.getMembers()).willReturn(Set.of(assignee));
        given(assignee.getId()).willReturn(3L);
        given(assignee.getUsername()).willReturn("ana");
        given(userRepository.findById(3L)).willReturn(Optional.of(assignee));
        given(proyectoRepository.findById(7L)).willReturn(Optional.of(project));

        assertThrows(IssueAssignmentException.class, () -> issueService.create(new IssueRequest(
                "Asignación ajena", null, IssueStatus.PENDIENTE, IssuePriority.MEDIA, 7L, 3L)));

        verify(issueRepository, never()).save(org.mockito.ArgumentMatchers.any(Issue.class));
    }

    private static void authenticateAs(String username, String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "n/a", List.of(new SimpleGrantedAuthority(authority))));
    }
}

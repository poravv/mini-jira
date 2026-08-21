package com.minijira.issue.service;

import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.Issue;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.issue.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private IssueService issueService;

    @Test
    void should_list_all_issues_when_no_filter_is_given() {
        given(issueRepository.search(null, null)).willReturn(List.of(issue("Caida del login", IssuePriority.ALTA)));

        List<IssueResponse> issues = issueService.findAll(null, null);

        assertEquals(1, issues.size());
        assertEquals("Caida del login", issues.get(0).title());
    }

    @Test
    void should_only_return_issues_matching_the_filter() {
        given(issueRepository.search(IssueStatus.PENDIENTE, IssuePriority.ALTA))
                .willReturn(List.of(issue("Caida del login", IssuePriority.ALTA)));

        List<IssueResponse> issues = issueService.findAll(IssueStatus.PENDIENTE, IssuePriority.ALTA);

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
}

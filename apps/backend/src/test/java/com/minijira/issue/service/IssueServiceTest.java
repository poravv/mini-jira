package com.minijira.issue.service;

import com.minijira.issue.entity.Issue;
import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.issue.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

package com.minijira.issue.controller;

import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.issue.service.IssueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IssueController.class)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueService issueService;

    @Test
    void should_return_issues_when_listing() throws Exception {
        IssueResponse issue = new IssueResponse(1L, "Fix login", null,
                IssueStatus.PENDIENTE, IssuePriority.MEDIA, Instant.now(), Instant.now());
        given(issueService.findAll()).willReturn(List.of(issue));

        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Fix login"))
                .andExpect(jsonPath("$[0].status").value("PENDIENTE"));
    }

    @Test
    void should_return_400_with_field_errors_when_title_is_blank() throws Exception {
        mockMvc.perform(post("/api/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void should_update_an_issue() throws Exception {
        IssueResponse updatedIssue = new IssueResponse(1L, "Fix registration", "Updated description",
                IssueStatus.EN_PROGRESO, IssuePriority.ALTA, Instant.now(), Instant.now());
        given(issueService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .willReturn(updatedIssue);

        mockMvc.perform(put("/api/issues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Fix registration\",\"description\":\"Updated description\",\"status\":\"EN_PROGRESO\",\"priority\":\"ALTA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Fix registration"))
                .andExpect(jsonPath("$.status").value("EN_PROGRESO"));
    }

    @Test
    void should_return_204_when_deleting_an_existing_issue() throws Exception {
        mockMvc.perform(delete("/api/issues/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(issueService).deleteById(1L);
    }

    @Test
    void should_return_404_when_deleting_a_missing_issue() throws Exception {
        willThrow(new IssueNotFoundException(99L))
                .given(issueService).deleteById(99L);

        mockMvc.perform(delete("/api/issues/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}

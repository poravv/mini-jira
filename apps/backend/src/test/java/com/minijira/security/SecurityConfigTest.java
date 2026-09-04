package com.minijira.security;

import com.minijira.issue.controller.IssueController;
import com.minijira.issue.service.IssueService;
import com.minijira.user.controller.UserController;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest({IssueController.class, UserController.class})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private UserService userService;

    @Test
    void should_reject_protected_endpoint_without_token() throws Exception {
        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_reject_an_invalid_bearer_token_with_json_error() throws Exception {
        mockMvc.perform(get("/api/issues")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_allow_authenticated_user_to_read_issues() throws Exception {
        given(issueService.findAll(null, null)).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/issues"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_restrict_user_management_to_admins() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}

package com.minijira.auth.config;

import com.minijira.auth.service.JwtService;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void should_allow_swagger_entrypoint_without_a_token() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_require_a_token_for_protected_endpoints() throws Exception {
        mockMvc.perform(get("/protected-resource"))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class TestController {
        @GetMapping("/swagger-ui.html")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        void swaggerEntrypoint() {
        }

        @GetMapping("/protected-resource")
        void protectedResource() {
        }
    }
}

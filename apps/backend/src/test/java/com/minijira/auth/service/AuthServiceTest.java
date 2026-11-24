package com.minijira.auth.service;

import com.minijira.auth.dto.LoginRequest;
import com.minijira.auth.dto.LoginResponse;
import com.minijira.auth.exception.AuthenticationException;
import com.minijira.user.entity.UserRole;
import com.minijira.user.service.UserAuthenticationData;
import com.minijira.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    @Test
    void should_issue_a_jwt_for_an_active_user_with_valid_password() {
        LoginRequest request = new LoginRequest("ana", "secreto123");
        UserAuthenticationData user = new UserAuthenticationData(1L, "ana", "bcrypt", true, UserRole.SUPPORT);
        given(userService.findAuthenticationData("ana")).willReturn(user);
        given(passwordEncoder.matches("secreto123", "bcrypt")).willReturn(true);
        given(jwtService.createToken(new AuthenticatedUser(1L, "ana", UserRole.SUPPORT))).willReturn("signed-jwt");

        LoginResponse response = authService.login(request);

        assertEquals("signed-jwt", response.accessToken());
        assertEquals(UserRole.SUPPORT, response.role());
    }

    @Test
    void should_reject_an_inactive_user_without_checking_the_password() {
        given(userService.findAuthenticationData("ana")).willReturn(
                new UserAuthenticationData(1L, "ana", "bcrypt", false, UserRole.USER));

        assertThrows(AuthenticationException.class, () -> authService.login(new LoginRequest("ana", "secreto123")));
    }
}

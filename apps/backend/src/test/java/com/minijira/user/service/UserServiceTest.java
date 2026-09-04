package com.minijira.user.service;

import com.minijira.user.dto.UserCreateRequest;
import com.minijira.user.dto.UserLoginRequest;
import com.minijira.user.dto.UserStatusRequest;
import com.minijira.user.dto.UserResponse;
import com.minijira.user.entity.User;
import com.minijira.user.entity.UserRole;
import com.minijira.user.exception.UserConflictException;
import com.minijira.user.exception.UserAuthenticationException;
import com.minijira.user.exception.UserNotFoundException;
import com.minijira.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.minijira.security.JwtTokenService jwtTokenService;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @Test
    void should_hash_password_and_default_role_when_creating_user() {
        UserCreateRequest request = new UserCreateRequest(
                "Ana.Admin", "ANA@example.com", "secreto123", "Ana", "Admin", null);
        given(userRepository.existsByUsernameIgnoreCase("ana.admin")).willReturn(false);
        given(userRepository.existsByEmailIgnoreCase("ana@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request);

        assertEquals("ana.admin", response.username());
        assertEquals(UserRole.USER, response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_reject_duplicate_username() {
        given(userRepository.existsByUsernameIgnoreCase("ana.admin")).willReturn(true);

        assertThrows(UserConflictException.class, () -> userService.create(
                new UserCreateRequest("Ana.Admin", "ana@example.com", "secreto123", "Ana", "Admin", UserRole.USER)));
    }

    @Test
    void should_force_public_registration_to_the_user_role() {
        UserCreateRequest request = new UserCreateRequest(
                "ana", "ana@example.com", "secreto123", "Ana", "Admin", UserRole.ADMIN);
        given(userRepository.existsByUsernameIgnoreCase("ana")).willReturn(false);
        given(userRepository.existsByEmailIgnoreCase("ana@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        userService.create(request, false);

        verify(userRepository).save(argThat(user -> user.getRole() == UserRole.USER));
    }

    @Test
    void should_update_user_status() {
        User user = new User();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        UserResponse response = userService.updateStatus(1L, new UserStatusRequest(false));

        assertEquals(false, response.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void should_authenticate_active_user_with_email_and_password() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setUsername("ana");
        user.setPasswordHash(new BCryptPasswordEncoder().encode("secreto123"));
        user.setActive(true);
        given(userRepository.findByIdentifier("ana@example.com")).willReturn(Optional.of(user));

        UserResponse response = userService.login(new UserLoginRequest("ana@example.com", "secreto123"));

        assertEquals("ana@example.com", response.email());
    }

    @Test
    void should_reject_suspended_user_even_with_correct_password() {
        User user = new User();
        user.setPasswordHash(new BCryptPasswordEncoder().encode("secreto123"));
        user.setActive(false);
        given(userRepository.findByIdentifier("ana@example.com")).willReturn(Optional.of(user));

        assertThrows(UserAuthenticationException.class, () -> userService.login(
                new UserLoginRequest("ana@example.com", "secreto123")));
    }

    @Test
    void should_throw_when_user_does_not_exist() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(99L));
    }
}

package com.minijira.user.service;

import com.minijira.user.dto.UserCreateRequest;
import com.minijira.user.dto.UserLoginRequest;
import com.minijira.user.dto.UserResponse;
import com.minijira.user.dto.UserStatusRequest;
import com.minijira.user.dto.UserUpdateRequest;
import com.minijira.user.entity.User;
import com.minijira.user.entity.UserRole;
import com.minijira.user.exception.UserConflictException;
import com.minijira.user.exception.UserAuthenticationException;
import com.minijira.user.exception.UserNotFoundException;
import com.minijira.user.mapper.UserMapper;
import com.minijira.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Boolean active) {
        return userRepository.search(active).stream().map(UserMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getUser(id));
    }

    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {
        String identifier = normalize(request.identifier());
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(UserAuthenticationException::new);

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UserAuthenticationException();
        }

        log.info("User authenticated: id={}", user.getId());
        return UserMapper.toResponse(user);
    }

    public UserResponse create(UserCreateRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());
        ensureUnique(username, email, null);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(request.role() == null ? UserRole.USER : request.role());
        User saved = userRepository.save(user);
        log.info("User created: id={} role={}", saved.getId(), saved.getRole());
        return UserMapper.toResponse(saved);
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        String username = request.username() == null ? user.getUsername() : normalize(request.username());
        String email = request.email() == null ? user.getEmail() : normalize(request.email());
        ensureUnique(username, email, id);

        user.setUsername(username);
        user.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        User updated = userRepository.save(user);
        log.info("User updated: id={} role={}", updated.getId(), updated.getRole());
        return UserMapper.toResponse(updated);
    }

    public UserResponse updateStatus(Long id, UserStatusRequest request) {
        User user = getUser(id);
        user.setActive(request.isActive());
        User updated = userRepository.save(user);
        log.info("User status updated: id={} active={}", updated.getId(), updated.isActive());
        return UserMapper.toResponse(updated);
    }

    private void ensureUnique(String username, String email, Long currentId) {
        boolean usernameTaken = currentId == null
                ? userRepository.existsByUsernameIgnoreCase(username)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(username, currentId);
        if (usernameTaken) {
            throw new UserConflictException("Username is already in use");
        }

        boolean emailTaken = currentId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (emailTaken) {
            throw new UserConflictException("Email is already in use");
        }
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found: id={}", id);
            return new UserNotFoundException(id);
        });
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}

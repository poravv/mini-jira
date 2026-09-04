package com.minijira.auth.config;

import com.minijira.auth.service.JwtService;
import com.minijira.user.service.UserAuthenticationData;
import com.minijira.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/login", "/swagger-ui/**", "/v3/api-docs/**", "/api/weather").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, exception) -> response.sendError(HttpStatus.UNAUTHORIZED.value())))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

    @Component
    static class JwtAuthenticationFilter extends OncePerRequestFilter {
        private final JwtService jwtService;
        private final UserService userService;

        JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
            this.jwtService = jwtService;
            this.userService = userService;
        }
        @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    var tokenUser = jwtService.parse(header.substring(7));
                    UserAuthenticationData user = userService.findActiveAuthenticationData(tokenUser.id());
                    if (user == null) {
                        throw new IllegalArgumentException("Inactive user");
                    }
                    var authenticatedUser = new com.minijira.auth.service.AuthenticatedUser(user.id(), user.username(), user.role());
                    var authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.role().name())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (RuntimeException ignored) { SecurityContextHolder.clearContext(); }
            }
            chain.doFilter(request, response);
        }
    }
}

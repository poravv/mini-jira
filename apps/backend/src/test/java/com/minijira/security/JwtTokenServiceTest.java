package com.minijira.security;

import com.minijira.user.entity.User;
import com.minijira.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtTokenServiceTest {

    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "mini-jira-test-secret-key-with-at-least-32-bytes".getBytes());

    @Test
    void should_create_token_with_identity_and_role_claims() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        given(encoder.encode(any(JwtEncoderParameters.class))).willReturn(
                Jwt.withTokenValue("token").header("alg", "HS256").claim("sub", "token").build());
        JwtTokenService service = new JwtTokenService(encoder, "mini-jira", 3600);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setUsername("ana");
        user.setRole(UserRole.SUPPORT);

        assertEquals("token", service.createToken(user));

        var captor = org.mockito.ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertEquals("mini-jira", claims.getClaimAsString("iss"));
        assertEquals("ana", claims.getClaimAsString("sub"));
        assertEquals("SUPPORT", claims.getClaimAsString("role"));
        assertEquals(7L, ((Number) claims.getClaim("userId")).longValue());
        assertEquals(true, claims.getClaim("exp") instanceof Instant);
    }

    @Test
    void should_create_a_token_that_the_configured_decoder_accepts() {
        SecurityConfig config = new SecurityConfig();
        var secretKey = config.jwtSecretKey(TEST_SECRET);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        JwtDecoder decoder = config.jwtDecoder(secretKey, "mini-jira");
        JwtTokenService service = new JwtTokenService(encoder, "mini-jira", 3600);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setUsername("ana");
        user.setRole(UserRole.ADMIN);

        Jwt decoded = decoder.decode(service.createToken(user));

        assertEquals("ana", decoded.getSubject());
        assertEquals("ADMIN", decoded.getClaimAsString("role"));
        assertEquals(7L, ((Number) decoded.getClaim("userId")).longValue());
    }
}

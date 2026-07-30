package com.sweet.dessertsystem.security;

import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActiveAccountJwtConverterTests {
    private final UserMapper userMapper = mock(UserMapper.class);
    private final ActiveAccountJwtConverter converter = new ActiveAccountJwtConverter(userMapper);

    @Test
    void activeUserGetsRoleFromCurrentDatabaseRecord() {
        User user = user(5L, "alice", "USER", 1);
        when(userMapper.selectById(5L)).thenReturn(user);

        var authentication = converter.convert(jwt("5"));

        assertThat(authentication.getName()).isEqualTo("alice");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void disabledUserIsRejectedEvenWithExistingJwt() {
        when(userMapper.selectById(5L)).thenReturn(user(5L, "alice", "USER", 0));

        assertThatThrownBy(() -> converter.convert(jwt("5")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("账号已停用");
    }

    private Jwt jwt(String subject) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("username", "ignored")
                .claim("role", "ignored")
                .build();
    }

    private User user(Long id, String username, String role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}

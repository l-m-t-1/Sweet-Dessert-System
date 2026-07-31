package com.sweet.dessertsystem.auth;

import com.sweet.dessertsystem.dto.UserView;
import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {
    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenService tokenService;
    @InjectMocks AuthService service;

    @Test
    void registerAlwaysCreatesEnabledUserWithEncodedPassword() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("secret12")).thenReturn("$2b$encoded");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, User.class).setId(7L);
            return 1;
        });

        UserView view = service.register(new RegisterRequest(" alice ", "secret12"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("$2b$encoded");
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(view).isEqualTo(new UserView(7L, "alice", "USER", 1, null));
    }

    @Test
    void duplicateUsernameIsRejected() {
        when(userMapper.selectOne(any())).thenReturn(user(2L, "alice", "$2b$hash", "USER", 1));

        assertThatThrownBy(() -> service.register(new RegisterRequest("alice", "secret12")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    void concurrentDuplicateInsertReturnsFriendlyError() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("secret12")).thenReturn("$2b$encoded");
        when(userMapper.insert(any(User.class)))
                .thenThrow(new DuplicateKeyException("uk_user_username"));

        assertThatThrownBy(() -> service.register(new RegisterRequest("alice", "secret12")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    void validPasswordReturnsSignedTokenAndUserSummary() {
        User stored = user(3L, "alice", "$2b$hash", "USER", 1);
        when(userMapper.selectOne(any())).thenReturn(stored);
        when(passwordEncoder.matches("secret12", "$2b$hash")).thenReturn(true);
        when(tokenService.issue(stored)).thenReturn("signed-token");

        AuthResponse response = service.login(new LoginRequest("alice", "secret12"));

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    void disabledAccountCannotLogin() {
        when(userMapper.selectOne(any()))
                .thenReturn(user(3L, "alice", "$2b$hash", "USER", 0));
        when(passwordEncoder.matches("secret12", "$2b$hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("alice", "secret12")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号已停用");
    }

    private User user(Long id, String username, String password, String role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}

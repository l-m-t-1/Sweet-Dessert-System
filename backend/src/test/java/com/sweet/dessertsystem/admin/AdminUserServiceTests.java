package com.sweet.dessertsystem.admin;

import com.sweet.dessertsystem.dto.UserView;
import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceTests {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final AdminUserService service = new AdminUserService(userMapper);

    @Test
    void listsSafeAccountSummaries() {
        User user = user(2L, "alice", "USER", 1);
        when(userMapper.countUsers("ali")).thenReturn(1L);
        when(userMapper.findUsers("ali", 0, 10)).thenReturn(List.of(user));

        AdminUserPageResult result = service.page(1, 10, " ali ");

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).containsExactly(UserView.from(user));
    }

    @Test
    void disablesARegularUser() {
        User user = user(2L, "alice", "USER", 1);
        when(userMapper.selectById(2L)).thenReturn(user);

        service.changeStatus(2L, 0, "admin");

        assertThat(user.getStatus()).isZero();
        verify(userMapper).updateById(user);
    }

    @Test
    void adminCannotDisableOwnAccount() {
        User admin = user(1L, "admin", "ADMIN", 1);
        when(userMapper.selectById(1L)).thenReturn(admin);

        assertThatThrownBy(() -> service.changeStatus(1L, 0, "admin"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsUnsupportedStatus() {
        assertThatThrownBy(() -> service.changeStatus(2L, 9, "admin"))
                .isInstanceOf(BusinessException.class);
    }

    private User user(Long id, String username, String role, int status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}

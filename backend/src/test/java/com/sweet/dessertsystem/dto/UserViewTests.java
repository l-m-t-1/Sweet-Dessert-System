package com.sweet.dessertsystem.dto;

import com.sweet.dessertsystem.entity.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UserViewTests {

    @Test
    void userViewContainsPublicFieldsWithoutPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("secret");
        user.setRole("ADMIN");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.of(2026, 7, 30, 10, 0));

        UserView view = UserView.from(user);

        assertThat(view.id()).isEqualTo(1L);
        assertThat(view.username()).isEqualTo("admin");
        assertThat(view.role()).isEqualTo("ADMIN");
        assertThat(view.status()).isEqualTo(1);
        assertThat(view.createTime()).isEqualTo(user.getCreateTime());
        assertThat(Arrays.stream(UserView.class.getRecordComponents())
                .map(RecordComponent::getName))
                .doesNotContain("password");
    }
}

package com.sweet.dessertsystem.dto;

import com.sweet.dessertsystem.entity.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
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

        UserView view = UserView.from(user);

        assertThat(view.id()).isEqualTo(1L);
        assertThat(view.username()).isEqualTo("admin");
        assertThat(view.role()).isEqualTo("ADMIN");
        assertThat(Arrays.stream(UserView.class.getRecordComponents())
                .map(RecordComponent::getName))
                .doesNotContain("password");
    }
}

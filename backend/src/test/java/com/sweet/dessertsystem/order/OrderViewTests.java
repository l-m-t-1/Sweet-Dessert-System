package com.sweet.dessertsystem.order;

import org.apache.ibatis.annotations.AutomapConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class OrderViewTests {

    @Test
    void exposesNineColumnConstructorForMyBatisOrderQueries() {
        assertThatCode(() -> {
            var constructor = OrderView.class.getDeclaredConstructor(
                    Long.class, String.class, String.class, String.class,
                    BigDecimal.class, String.class, String.class,
                    LocalDateTime.class, LocalDateTime.class);
            assertThat(constructor.isAnnotationPresent(AutomapConstructor.class)).isTrue();
        }).doesNotThrowAnyException();
    }
}

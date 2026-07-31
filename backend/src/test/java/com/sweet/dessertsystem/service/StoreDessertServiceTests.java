package com.sweet.dessertsystem.service;

import com.sweet.dessertsystem.category.CategoryMapper;
import com.sweet.dessertsystem.dto.DessertView;
import com.sweet.dessertsystem.mapper.DessertMapper;
import com.sweet.dessertsystem.service.impl.DessertServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreDessertServiceTests {
    private final DessertMapper dessertMapper = mock(DessertMapper.class);
    private final DessertService service = new DessertServiceImpl(
            dessertMapper, mock(CategoryMapper.class));

    @Test
    void availablePageUsesStoreOnlyQueries() {
        DessertView available = new DessertView(
                1L, "提拉米苏", 2L, "蛋糕", new BigDecimal("28.00"),
                9, null, null, 1, null, null);
        when(dessertMapper.countAvailablePage("提拉", 2L)).thenReturn(1L);
        when(dessertMapper.findAvailablePage("提拉", 2L, 0, 12))
                .thenReturn(List.of(available));

        var result = service.pageAvailable(1, 12, " 提拉 ", 2L);

        assertThat(result.records()).containsExactly(available);
        assertThat(result.total()).isEqualTo(1);
    }
}

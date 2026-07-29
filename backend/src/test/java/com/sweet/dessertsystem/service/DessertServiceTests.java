package com.sweet.dessertsystem.service;

import com.sweet.dessertsystem.category.CategoryMapper;
import com.sweet.dessertsystem.dto.DessertRequest;
import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import com.sweet.dessertsystem.service.impl.DessertServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DessertServiceTests {
    @Mock DessertMapper dessertMapper;
    @Mock CategoryMapper categoryMapper;
    @InjectMocks DessertServiceImpl service;

    @Test
    void rejectsNegativePrice() {
        DessertRequest request = new DessertRequest(
                "提拉米苏", 1L, new BigDecimal("-1.00"), 3, null, null, 1);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("价格不能小于0");
    }

    @Test
    void rejectsMissingCategory() {
        when(categoryMapper.selectById(99L)).thenReturn(null);
        DessertRequest request = new DessertRequest(
                "提拉米苏", 99L, new BigDecimal("28.00"), 3, null, null, 1);

        assertThatThrownBy(() -> service.create(request))
                .hasMessage("所选分类不存在");
    }

    @Test
    void togglesStatus() {
        Dessert dessert = new Dessert();
        dessert.setId(1L);
        dessert.setStatus(1);
        when(dessertMapper.selectById(1L)).thenReturn(dessert);

        service.changeStatus(1L, 0);

        verify(dessertMapper).updateById(argThat((Dessert item) -> item.getStatus() == 0));
    }
}

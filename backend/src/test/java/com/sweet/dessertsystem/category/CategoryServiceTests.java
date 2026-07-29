package com.sweet.dessertsystem.category;

import com.sweet.dessertsystem.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryService service;

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> service.create(new CategoryRequest("  ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分类名称不能为空");
    }

    @Test
    void rejectsDuplicateName() {
        when(mapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.create(new CategoryRequest("蛋糕")))
                .hasMessage("分类名称已存在");
    }

    @Test
    void rejectsDeletingUsedCategory() {
        when(mapper.selectById(2L)).thenReturn(new Category());
        when(mapper.countDesserts(2L)).thenReturn(1L);

        assertThatThrownBy(() -> service.delete(2L))
                .hasMessage("该分类下仍有甜品，不能删除");
    }
}

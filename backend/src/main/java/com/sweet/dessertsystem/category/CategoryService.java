package com.sweet.dessertsystem.category;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sweet.dessertsystem.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper mapper;

    public CategoryService(CategoryMapper mapper) {
        this.mapper = mapper;
    }

    public List<Category> list() {
        return mapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getName));
    }

    public Category create(CategoryRequest request) {
        String name = normalizeName(request);
        ensureUnique(name, null);
        Category category = new Category();
        category.setName(name);
        mapper.insert(category);
        return mapper.selectById(category.getId());
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = requireCategory(id);
        String name = normalizeName(request);
        ensureUnique(name, id);
        category.setName(name);
        mapper.updateById(category);
        return mapper.selectById(id);
    }

    public void delete(Long id) {
        requireCategory(id);
        if (mapper.countDesserts(id) > 0) {
            throw new BusinessException("该分类下仍有甜品，不能删除");
        }
        mapper.deleteById(id);
    }

    private Category requireCategory(Long id) {
        Category category = mapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    private String normalizeName(CategoryRequest request) {
        String name = request == null || request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
        if (name.length() > 50) {
            throw new BusinessException("分类名称不能超过50个字符");
        }
        return name;
    }

    private void ensureUnique(String name, Long excludedId) {
        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name);
        if (excludedId != null) {
            query.ne(Category::getId, excludedId);
        }
        if (mapper.selectCount(query) > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }
}

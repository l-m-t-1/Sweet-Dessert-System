package com.sweet.dessertsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sweet.dessertsystem.category.CategoryMapper;
import com.sweet.dessertsystem.dto.DessertPageResult;
import com.sweet.dessertsystem.dto.DessertRequest;
import com.sweet.dessertsystem.dto.DessertView;
import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import com.sweet.dessertsystem.service.DessertService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DessertServiceImpl extends ServiceImpl<DessertMapper, Dessert>
        implements DessertService {
    private final DessertMapper dessertMapper;
    private final CategoryMapper categoryMapper;

    public DessertServiceImpl(DessertMapper dessertMapper, CategoryMapper categoryMapper) {
        this.dessertMapper = dessertMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public DessertPageResult page(long page, long size, String name, Long categoryId) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String keyword = name == null ? null : name.trim();
        long total = dessertMapper.countPage(keyword, categoryId);
        return new DessertPageResult(
                dessertMapper.findPage(keyword, categoryId, (safePage - 1) * safeSize, safeSize),
                total, safePage, safeSize);
    }

    @Override
    public DessertPageResult pageAvailable(long page, long size, String name, Long categoryId) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String keyword = name == null ? null : name.trim();
        long total = dessertMapper.countAvailablePage(keyword, categoryId);
        return new DessertPageResult(
                dessertMapper.findAvailablePage(keyword, categoryId,
                        (safePage - 1) * safeSize, safeSize),
                total, safePage, safeSize);
    }

    @Override
    public DessertView create(DessertRequest request) {
        validate(request);
        Dessert dessert = copy(request, new Dessert());
        dessertMapper.insert(dessert);
        return dessertMapper.findViewById(dessert.getId());
    }

    @Override
    public DessertView update(Long id, DessertRequest request) {
        Dessert dessert = requireDessert(id);
        validate(request);
        dessertMapper.updateById(copy(request, dessert));
        return dessertMapper.findViewById(id);
    }

    @Override
    public void delete(Long id) {
        requireDessert(id);
        dessertMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Dessert dessert = requireDessert(id);
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("上下架状态只能是0或1");
        }
        dessert.setStatus(status);
        dessertMapper.updateById(dessert);
    }

    private void validate(DessertRequest request) {
        if (request == null || request.name() == null || request.name().trim().isEmpty()) {
            throw new BusinessException("甜品名称不能为空");
        }
        if (request.name().trim().length() > 100) {
            throw new BusinessException("甜品名称不能超过100个字符");
        }
        if (request.price() == null) {
            throw new BusinessException("请输入价格");
        }
        if (request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("价格不能小于0");
        }
        if (request.stock() == null || request.stock() < 0) {
            throw new BusinessException("库存必须是非负整数");
        }
        if (request.categoryId() == null || categoryMapper.selectById(request.categoryId()) == null) {
            throw new BusinessException("所选分类不存在");
        }
        if (request.status() != null && request.status() != 0 && request.status() != 1) {
            throw new BusinessException("上下架状态只能是0或1");
        }
    }

    private Dessert requireDessert(Long id) {
        Dessert dessert = dessertMapper.selectById(id);
        if (dessert == null) {
            throw new BusinessException("甜品不存在");
        }
        return dessert;
    }

    private Dessert copy(DessertRequest request, Dessert dessert) {
        dessert.setName(request.name().trim());
        dessert.setCategoryId(request.categoryId());
        dessert.setPrice(request.price());
        dessert.setStock(request.stock());
        dessert.setImage(request.image());
        dessert.setDescription(request.description());
        dessert.setStatus(request.status() == null ? 1 : request.status());
        return dessert;
    }
}

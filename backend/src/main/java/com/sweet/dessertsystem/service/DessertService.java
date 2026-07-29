package com.sweet.dessertsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sweet.dessertsystem.dto.DessertPageResult;
import com.sweet.dessertsystem.dto.DessertRequest;
import com.sweet.dessertsystem.dto.DessertView;
import com.sweet.dessertsystem.entity.Dessert;


public interface DessertService extends IService<Dessert> {
    DessertPageResult page(long page, long size, String name, Long categoryId);
    DessertView create(DessertRequest request);
    DessertView update(Long id, DessertRequest request);
    void delete(Long id);
    void changeStatus(Long id, Integer status);
}

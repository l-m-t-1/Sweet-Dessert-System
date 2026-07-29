package com.sweet.dessertsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DessertView(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal price,
        Integer stock,
        String image,
        String description,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}

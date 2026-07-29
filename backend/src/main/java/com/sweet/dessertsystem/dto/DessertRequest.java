package com.sweet.dessertsystem.dto;

import java.math.BigDecimal;

public record DessertRequest(
        String name,
        Long categoryId,
        BigDecimal price,
        Integer stock,
        String image,
        String description,
        Integer status
) {
}

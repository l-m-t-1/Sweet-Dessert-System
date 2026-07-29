package com.sweet.dessertsystem.order;

import java.math.BigDecimal;

public record OrderItemView(
        Long id,
        Long dessertId,
        String dessertName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal) {
}

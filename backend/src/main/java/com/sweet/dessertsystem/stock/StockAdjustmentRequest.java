package com.sweet.dessertsystem.stock;

public record StockAdjustmentRequest(
        Long dessertId,
        String direction,
        Integer quantity,
        String remark) {
}

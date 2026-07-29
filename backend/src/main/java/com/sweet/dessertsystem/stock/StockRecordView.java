package com.sweet.dessertsystem.stock;

import java.time.LocalDateTime;

public record StockRecordView(
        Long id,
        Long dessertId,
        String dessertName,
        Long orderId,
        String orderNo,
        Integer changeQuantity,
        Integer beforeStock,
        Integer afterStock,
        String type,
        String remark,
        LocalDateTime createTime) {
}

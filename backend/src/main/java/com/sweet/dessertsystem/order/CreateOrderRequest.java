package com.sweet.dessertsystem.order;

import java.util.List;

public record CreateOrderRequest(
        String customerName,
        String customerPhone,
        String remark,
        List<CreateOrderItemRequest> items) {
}

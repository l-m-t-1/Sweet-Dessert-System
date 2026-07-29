package com.sweet.dessertsystem.order;

public record CreateOrderItemRequest(Long dessertId, Integer quantity) {
}

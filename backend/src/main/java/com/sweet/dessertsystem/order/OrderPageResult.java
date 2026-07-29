package com.sweet.dessertsystem.order;

import java.util.List;

public record OrderPageResult(
        List<OrderView> records,
        long total,
        long page,
        long size) {
}

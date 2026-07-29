package com.sweet.dessertsystem.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentOrder(
        Long id,
        String orderNo,
        String customerName,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createTime) {
}

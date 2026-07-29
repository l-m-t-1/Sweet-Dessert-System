package com.sweet.dessertsystem.dashboard;

import java.util.List;

public record DashboardSummary(
        long dessertCount,
        long categoryCount,
        long totalStock,
        long lowStockCount,
        List<LowStockDessert> lowStockDesserts
) {
}

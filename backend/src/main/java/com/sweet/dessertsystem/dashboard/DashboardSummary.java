package com.sweet.dessertsystem.dashboard;

import java.util.List;
import java.math.BigDecimal;

public record DashboardSummary(
        long dessertCount,
        long categoryCount,
        long totalStock,
        long lowStockCount,
        List<LowStockDessert> lowStockDesserts,
        long todayOrderCount,
        BigDecimal todaySalesAmount,
        List<RecentOrder> recentOrders
) {
}

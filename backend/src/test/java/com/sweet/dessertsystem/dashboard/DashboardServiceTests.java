package com.sweet.dessertsystem.dashboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {
    @Mock DashboardMapper mapper;
    @InjectMocks DashboardService service;

    @Test
    void returnsAllDashboardMetrics() {
        when(mapper.countDesserts()).thenReturn(12L);
        when(mapper.countCategories()).thenReturn(4L);
        when(mapper.sumStock()).thenReturn(86L);
        when(mapper.countLowStock(5)).thenReturn(2L);
        when(mapper.findLowStock(5, 5)).thenReturn(List.of());
        when(mapper.countTodayOrders()).thenReturn(3L);
        when(mapper.sumTodaySales()).thenReturn(new BigDecimal("128.50"));
        when(mapper.findRecentOrders(5)).thenReturn(List.of());

        DashboardSummary result = service.summary();

        assertThat(result.dessertCount()).isEqualTo(12);
        assertThat(result.categoryCount()).isEqualTo(4);
        assertThat(result.totalStock()).isEqualTo(86);
        assertThat(result.lowStockCount()).isEqualTo(2);
        assertThat(result.todayOrderCount()).isEqualTo(3);
        assertThat(result.todaySalesAmount()).isEqualByComparingTo("128.50");
        assertThat(result.recentOrders()).isEmpty();
    }
}

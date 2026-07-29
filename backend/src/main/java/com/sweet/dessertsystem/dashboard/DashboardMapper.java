package com.sweet.dessertsystem.dashboard;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.math.BigDecimal;

public interface DashboardMapper {
    @Select("SELECT COUNT(*) FROM dessert")
    long countDesserts();

    @Select("SELECT COUNT(*) FROM category")
    long countCategories();

    @Select("SELECT COALESCE(SUM(stock), 0) FROM dessert")
    long sumStock();

    @Select("SELECT COUNT(*) FROM dessert WHERE stock <= #{threshold}")
    long countLowStock(@Param("threshold") int threshold);

    @Select("""
            SELECT id, name, stock, image
            FROM dessert
            WHERE stock <= #{threshold}
            ORDER BY stock ASC, update_time DESC
            LIMIT #{limit}
            """)
    List<LowStockDessert> findLowStock(@Param("threshold") int threshold,
                                      @Param("limit") int limit);

    @Select("""
            SELECT COUNT(*) FROM orders
            WHERE create_time >= CURRENT_DATE
              AND create_time < CURRENT_DATE + INTERVAL 1 DAY
            """)
    long countTodayOrders();

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0) FROM orders
            WHERE create_time >= CURRENT_DATE
              AND create_time < CURRENT_DATE + INTERVAL 1 DAY
              AND status <> 'CANCELLED'
            """)
    BigDecimal sumTodaySales();

    @Select("""
            SELECT id, order_no, customer_name, total_amount, status, create_time
            FROM orders
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            """)
    List<RecentOrder> findRecentOrders(@Param("limit") int limit);
}

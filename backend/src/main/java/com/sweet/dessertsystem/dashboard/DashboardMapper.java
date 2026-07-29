package com.sweet.dessertsystem.dashboard;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}

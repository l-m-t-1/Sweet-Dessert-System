package com.sweet.dessertsystem.stock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {

    @Select("""
            <script>
            SELECT sr.id, sr.dessert_id, d.name AS dessert_name,
                   sr.order_id, o.order_no, sr.change_quantity,
                   sr.before_stock, sr.after_stock, sr.type,
                   sr.remark, sr.create_time
            FROM stock_record sr
            JOIN dessert d ON d.id = sr.dessert_id
            LEFT JOIN orders o ON o.id = sr.order_id
            WHERE 1 = 1
            <if test='dessertId != null'>
              AND sr.dessert_id = #{dessertId}
            </if>
            <if test='type != null and type != ""'>
              AND sr.type = #{type}
            </if>
            ORDER BY sr.create_time DESC, sr.id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<StockRecordView> findPage(@Param("dessertId") Long dessertId,
                                   @Param("type") String type,
                                   @Param("offset") long offset,
                                   @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM stock_record sr
            WHERE 1 = 1
            <if test='dessertId != null'>
              AND sr.dessert_id = #{dessertId}
            </if>
            <if test='type != null and type != ""'>
              AND sr.type = #{type}
            </if>
            </script>
            """)
    long countPage(@Param("dessertId") Long dessertId, @Param("type") String type);

    @Select("""
            SELECT sr.id, sr.dessert_id, d.name AS dessert_name,
                   sr.order_id, o.order_no, sr.change_quantity,
                   sr.before_stock, sr.after_stock, sr.type,
                   sr.remark, sr.create_time
            FROM stock_record sr
            JOIN dessert d ON d.id = sr.dessert_id
            LEFT JOIN orders o ON o.id = sr.order_id
            WHERE sr.id = #{id}
            """)
    StockRecordView findViewById(Long id);
}

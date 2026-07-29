package com.sweet.dessertsystem.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE id = #{id} FOR UPDATE")
    Order findByIdForUpdate(Long id);

    @Select("""
            <script>
            SELECT id, order_no, customer_name, customer_phone, total_amount,
                   status, remark, create_time, update_time
            FROM orders
            WHERE 1 = 1
            <if test='orderNo != null and orderNo != ""'>
              AND order_no LIKE CONCAT('%', #{orderNo}, '%')
            </if>
            <if test='status != null and status != ""'>
              AND status = #{status}
            </if>
            ORDER BY create_time DESC, id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<OrderView> findPage(@Param("orderNo") String orderNo,
                             @Param("status") String status,
                             @Param("offset") long offset,
                             @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM orders
            WHERE 1 = 1
            <if test='orderNo != null and orderNo != ""'>
              AND order_no LIKE CONCAT('%', #{orderNo}, '%')
            </if>
            <if test='status != null and status != ""'>
              AND status = #{status}
            </if>
            </script>
            """)
    long countPage(@Param("orderNo") String orderNo, @Param("status") String status);

    @Select("""
            SELECT id, order_no, customer_name, customer_phone, total_amount,
                   status, remark, create_time, update_time
            FROM orders WHERE id = #{id}
            """)
    OrderView findViewById(Long id);
}

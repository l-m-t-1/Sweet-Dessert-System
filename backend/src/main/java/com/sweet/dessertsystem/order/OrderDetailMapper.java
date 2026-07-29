package com.sweet.dessertsystem.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId} ORDER BY id")
    List<OrderDetail> selectByOrderId(Long orderId);

    @Select("""
            SELECT id, dessert_id, dessert_name, unit_price, quantity, subtotal
            FROM order_detail
            WHERE order_id = #{orderId}
            ORDER BY id
            """)
    List<OrderItemView> findViewsByOrderId(Long orderId);
}

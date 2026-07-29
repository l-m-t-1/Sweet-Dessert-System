package com.sweet.dessertsystem.order;

import org.apache.ibatis.annotations.AutomapConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderView(
        Long id,
        String orderNo,
        String customerName,
        String customerPhone,
        BigDecimal totalAmount,
        String status,
        String remark,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        List<OrderItemView> items) {

    @AutomapConstructor
    public OrderView(Long id,
                     String orderNo,
                     String customerName,
                     String customerPhone,
                     BigDecimal totalAmount,
                     String status,
                     String remark,
                     LocalDateTime createTime,
                     LocalDateTime updateTime) {
        this(id, orderNo, customerName, customerPhone, totalAmount,
                status, remark, createTime, updateTime, List.of());
    }

    public OrderView withItems(List<OrderItemView> orderItems) {
        return new OrderView(id, orderNo, customerName, customerPhone,
                totalAmount, status, remark, createTime, updateTime, orderItems);
    }
}

package com.sweet.dessertsystem.order;

import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import com.sweet.dessertsystem.stock.StockRecord;
import com.sweet.dessertsystem.stock.StockRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {
    @Mock OrderMapper orderMapper;
    @Mock OrderDetailMapper orderDetailMapper;
    @Mock DessertMapper dessertMapper;
    @Mock StockRecordMapper stockRecordMapper;

    @Test
    void createsOrderAndDeductsInventoryInOneWorkflow() {
        OrderService service = service();
        Dessert dessert = dessert(1L, "提拉米苏", "28.00", 10, 1);
        when(dessertMapper.findByIdForUpdate(1L)).thenReturn(dessert);
        when(dessertMapper.updateById(any(Dessert.class))).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return 1;
        });

        service.create(new CreateOrderRequest(
                "张三", "13800000000", "少糖",
                List.of(new CreateOrderItemRequest(1L, 2))));

        verify(orderMapper).insert(argThat((Order order) ->
                order.getTotalAmount().compareTo(new BigDecimal("56.00")) == 0
                        && order.getStatus().equals("CREATED")));
        verify(orderDetailMapper).insert(argThat((OrderDetail detail) ->
                detail.getOrderId().equals(100L)
                        && detail.getDessertName().equals("提拉米苏")
                        && detail.getQuantity() == 2
                        && detail.getSubtotal().compareTo(new BigDecimal("56.00")) == 0));
        verify(dessertMapper).updateById(argThat((Dessert item) -> item.getStock() == 8));
        verify(stockRecordMapper).insert(argThat((StockRecord record) ->
                record.getOrderId().equals(100L)
                        && record.getType().equals("ORDER_OUT")
                        && record.getChangeQuantity() == -2));
    }

    @Test
    void rejectsOrderWhenInventoryIsInsufficient() {
        OrderService service = service();
        when(dessertMapper.findByIdForUpdate(1L))
                .thenReturn(dessert(1L, "提拉米苏", "28.00", 1, 1));

        assertThatThrownBy(() -> service.create(new CreateOrderRequest(
                "张三", null, null,
                List.of(new CreateOrderItemRequest(1L, 2)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("提拉米苏库存不足");

        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void rejectsUnavailableDessert() {
        OrderService service = service();
        when(dessertMapper.findByIdForUpdate(1L))
                .thenReturn(dessert(1L, "提拉米苏", "28.00", 10, 0));

        assertThatThrownBy(() -> service.create(new CreateOrderRequest(
                "张三", null, null,
                List.of(new CreateOrderItemRequest(1L, 1)))))
                .hasMessage("提拉米苏已下架");
    }

    @Test
    void cancellationReturnsInventoryAndRecordsHistory() {
        OrderService service = service();
        Order order = order(100L, "CREATED");
        OrderDetail detail = detail(100L, 1L, 2);
        when(orderMapper.findByIdForUpdate(100L)).thenReturn(order);
        when(orderDetailMapper.selectByOrderId(100L)).thenReturn(List.of(detail));
        when(dessertMapper.findByIdForUpdate(1L))
                .thenReturn(dessert(1L, "提拉米苏", "28.00", 8, 1));
        when(dessertMapper.updateById(any(Dessert.class))).thenReturn(1);

        service.cancel(100L);

        verify(dessertMapper).updateById(argThat((Dessert item) -> item.getStock() == 10));
        verify(stockRecordMapper).insert(argThat((StockRecord record) ->
                record.getType().equals("ORDER_RETURN")
                        && record.getChangeQuantity() == 2));
        verify(orderMapper).updateById(argThat((Order item) ->
                item.getStatus().equals("CANCELLED")));
    }

    @Test
    void rejectsRepeatedCancellation() {
        OrderService service = service();
        when(orderMapper.findByIdForUpdate(100L)).thenReturn(order(100L, "CANCELLED"));

        assertThatThrownBy(() -> service.cancel(100L))
                .hasMessage("只有已创建订单可以取消");
    }

    @Test
    void completesCreatedOrder() {
        OrderService service = service();
        when(orderMapper.findByIdForUpdate(100L)).thenReturn(order(100L, "CREATED"));

        service.complete(100L);

        verify(orderMapper).updateById(argThat((Order item) ->
                item.getStatus().equals("COMPLETED")));
    }

    private OrderService service() {
        return new OrderService(orderMapper, orderDetailMapper, dessertMapper, stockRecordMapper);
    }

    private Dessert dessert(Long id, String name, String price, int stock, int status) {
        Dessert dessert = new Dessert();
        dessert.setId(id);
        dessert.setName(name);
        dessert.setPrice(new BigDecimal(price));
        dessert.setStock(stock);
        dessert.setStatus(status);
        return dessert;
    }

    private Order order(Long id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("DS100");
        order.setStatus(status);
        return order;
    }

    private OrderDetail detail(Long orderId, Long dessertId, int quantity) {
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setDessertId(dessertId);
        detail.setQuantity(quantity);
        return detail;
    }
}

package com.sweet.dessertsystem.order;

import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import com.sweet.dessertsystem.stock.StockRecordMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerOrderServiceTests {
    private final OrderMapper orderMapper = mock(OrderMapper.class);
    private final OrderDetailMapper orderDetailMapper = mock(OrderDetailMapper.class);
    private final DessertMapper dessertMapper = mock(DessertMapper.class);
    private final StockRecordMapper stockRecordMapper = mock(StockRecordMapper.class);
    private final OrderService service = new OrderService(
            orderMapper, orderDetailMapper, dessertMapper, stockRecordMapper);

    @Test
    void customerOrderUsesAuthenticatedIdentityAndStoresOwner() {
        Dessert dessert = new Dessert();
        dessert.setId(1L);
        dessert.setName("提拉米苏");
        dessert.setPrice(new BigDecimal("28.00"));
        dessert.setStock(10);
        dessert.setStatus(1);
        when(dessertMapper.findByIdForUpdate(1L)).thenReturn(dessert);
        when(dessertMapper.updateById(any(Dessert.class))).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Order.class).setId(100L);
            return 1;
        });

        service.createForUser(new CreateOrderRequest(
                "伪造客户名", null, null,
                List.of(new CreateOrderItemRequest(1L, 1))),
                9L, "alice");

        verify(orderMapper).insert(argThat((Order order) ->
                order.getUserId().equals(9L)
                        && order.getCustomerName().equals("alice")));
    }

    @Test
    void customerCannotReadAnotherUsersOrder() {
        when(orderMapper.findViewByIdAndUserId(8L, 9L)).thenReturn(null);

        assertThatThrownBy(() -> service.detailForUser(8L, 9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权访问该订单");
    }

    @Test
    void customerCannotCancelAnotherUsersOrder() {
        when(orderMapper.findByIdAndUserIdForUpdate(8L, 9L)).thenReturn(null);

        assertThatThrownBy(() -> service.cancelForUser(8L, 9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权访问该订单");
    }
}

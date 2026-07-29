package com.sweet.dessertsystem.stock;

import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockRecordServiceTests {
    @Mock DessertMapper dessertMapper;
    @Mock StockRecordMapper stockRecordMapper;
    @InjectMocks StockRecordService service;

    @Test
    void recordsManualInboundAdjustment() {
        Dessert dessert = dessert(1L, 10);
        when(dessertMapper.findByIdForUpdate(1L)).thenReturn(dessert);
        when(dessertMapper.updateById(any(Dessert.class))).thenReturn(1);

        service.adjust(new StockAdjustmentRequest(1L, "IN", 3, "采购入库"));

        verify(dessertMapper).updateById(argThat((Dessert item) -> item.getStock() == 13));
        verify(stockRecordMapper).insert(argThat((StockRecord record) ->
                record.getDessertId().equals(1L)
                        && record.getChangeQuantity() == 3
                        && record.getBeforeStock() == 10
                        && record.getAfterStock() == 13
                        && record.getType().equals("MANUAL_IN")
                        && record.getRemark().equals("采购入库")));
    }

    @Test
    void rejectsOutboundAdjustmentWhenStockIsInsufficient() {
        when(dessertMapper.findByIdForUpdate(1L)).thenReturn(dessert(1L, 2));

        assertThatThrownBy(() ->
                service.adjust(new StockAdjustmentRequest(1L, "OUT", 3, "门店损耗")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("库存不足");

        verify(dessertMapper, never()).updateById(any(Dessert.class));
        verifyNoInteractions(stockRecordMapper);
    }

    @Test
    void requiresAdjustmentRemarkBeforeDatabaseAccess() {
        assertThatThrownBy(() ->
                service.adjust(new StockAdjustmentRequest(1L, "IN", 3, " ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请填写库存调整原因");

        verifyNoInteractions(dessertMapper, stockRecordMapper);
    }

    private Dessert dessert(Long id, int stock) {
        Dessert dessert = new Dessert();
        dessert.setId(id);
        dessert.setStock(stock);
        dessert.setName("测试甜品");
        return dessert;
    }
}

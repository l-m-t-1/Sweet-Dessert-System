package com.sweet.dessertsystem.stock;

import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class StockRecordService {
    private static final Set<String> TYPES = Set.of(
            "MANUAL_IN", "MANUAL_OUT", "ORDER_OUT", "ORDER_RETURN");

    private final DessertMapper dessertMapper;
    private final StockRecordMapper stockRecordMapper;

    public StockRecordService(DessertMapper dessertMapper,
                              StockRecordMapper stockRecordMapper) {
        this.dessertMapper = dessertMapper;
        this.stockRecordMapper = stockRecordMapper;
    }

    public StockRecordPageResult page(long page, long size, Long dessertId, String type) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String safeType = normalizeType(type);
        long total = stockRecordMapper.countPage(dessertId, safeType);
        return new StockRecordPageResult(
                stockRecordMapper.findPage(
                        dessertId, safeType, (safePage - 1) * safeSize, safeSize),
                total, safePage, safeSize);
    }

    @Transactional
    public StockRecordView adjust(StockAdjustmentRequest request) {
        validate(request);
        Dessert dessert = dessertMapper.findByIdForUpdate(request.dessertId());
        if (dessert == null) {
            throw new BusinessException("甜品不存在");
        }

        int quantity = request.quantity();
        int change = "IN".equals(request.direction().trim().toUpperCase())
                ? quantity : -quantity;
        int before = dessert.getStock() == null ? 0 : dessert.getStock();
        int after = before + change;
        if (after < 0) {
            throw new BusinessException("库存不足");
        }

        dessert.setStock(after);
        if (dessertMapper.updateById(dessert) != 1) {
            throw new BusinessException("库存更新失败，请重试");
        }

        StockRecord record = new StockRecord();
        record.setDessertId(dessert.getId());
        record.setChangeQuantity(change);
        record.setBeforeStock(before);
        record.setAfterStock(after);
        record.setType(change > 0 ? "MANUAL_IN" : "MANUAL_OUT");
        record.setRemark(request.remark().trim());
        stockRecordMapper.insert(record);
        return stockRecordMapper.findViewById(record.getId());
    }

    private void validate(StockAdjustmentRequest request) {
        if (request == null || request.remark() == null || request.remark().trim().isEmpty()) {
            throw new BusinessException("请填写库存调整原因");
        }
        if (request.dessertId() == null) {
            throw new BusinessException("请选择甜品");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException("调整数量必须大于0");
        }
        if (request.direction() == null) {
            throw new BusinessException("请选择入库或出库");
        }
        String direction = request.direction().trim().toUpperCase();
        if (!"IN".equals(direction) && !"OUT".equals(direction)) {
            throw new BusinessException("库存调整方向只能是IN或OUT");
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        String value = type.trim().toUpperCase();
        if (!TYPES.contains(value)) {
            throw new BusinessException("库存流水类型无效");
        }
        return value;
    }
}

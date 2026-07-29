package com.sweet.dessertsystem.stock;

import java.util.List;

public record StockRecordPageResult(
        List<StockRecordView> records,
        long total,
        long page,
        long size) {
}

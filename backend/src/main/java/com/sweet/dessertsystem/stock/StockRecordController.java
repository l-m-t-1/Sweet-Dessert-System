package com.sweet.dessertsystem.stock;

import com.sweet.dessertsystem.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-records")
public class StockRecordController {
    private final StockRecordService stockRecordService;

    public StockRecordController(StockRecordService stockRecordService) {
        this.stockRecordService = stockRecordService;
    }

    @GetMapping
    public ApiResponse<StockRecordPageResult> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long dessertId,
            @RequestParam(required = false) String type) {
        return ApiResponse.ok(stockRecordService.page(page, size, dessertId, type));
    }

    @PostMapping("/adjust")
    public ApiResponse<StockRecordView> adjust(
            @RequestBody StockAdjustmentRequest request) {
        return ApiResponse.ok(stockRecordService.adjust(request));
    }
}

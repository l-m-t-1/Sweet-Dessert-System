package com.sweet.dessertsystem.store;

import com.sweet.dessertsystem.common.ApiResponse;
import com.sweet.dessertsystem.dto.DessertPageResult;
import com.sweet.dessertsystem.service.DessertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    private final DessertService dessertService;

    public StoreController(DessertService dessertService) {
        this.dessertService = dessertService;
    }

    @GetMapping("/desserts")
    public ApiResponse<DessertPageResult> desserts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(dessertService.pageAvailable(page, size, name, categoryId));
    }
}

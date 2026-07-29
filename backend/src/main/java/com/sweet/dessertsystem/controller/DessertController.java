package com.sweet.dessertsystem.controller;

import com.sweet.dessertsystem.common.ApiResponse;
import com.sweet.dessertsystem.dto.DessertPageResult;
import com.sweet.dessertsystem.dto.DessertRequest;
import com.sweet.dessertsystem.dto.DessertView;
import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.service.DessertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dessert")
public class DessertController {
    private final DessertService dessertService;

    public DessertController(DessertService dessertService) {
        this.dessertService = dessertService;
    }

    @GetMapping("/list")
    public List<Dessert> list() {
        return dessertService.list();
    }

    @GetMapping("/page")
    public ApiResponse<DessertPageResult> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(dessertService.page(page, size, name, categoryId));
    }

    @PostMapping
    public ApiResponse<DessertView> create(@RequestBody DessertRequest request) {
        return ApiResponse.ok(dessertService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DessertView> update(@PathVariable Long id, @RequestBody DessertRequest request) {
        return ApiResponse.ok(dessertService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dessertService.delete(id);
        return ApiResponse.ok();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Integer> body) {
        dessertService.changeStatus(id, body.get("status"));
        return ApiResponse.ok();
    }
}

package com.sweet.dessertsystem.web;

import com.sweet.dessertsystem.category.CategoryController;
import com.sweet.dessertsystem.controller.DessertController;
import com.sweet.dessertsystem.controller.UserController;
import com.sweet.dessertsystem.dashboard.DashboardController;
import com.sweet.dessertsystem.order.OrderController;
import com.sweet.dessertsystem.stock.StockRecordController;
import com.sweet.dessertsystem.upload.UploadController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ApiPrefixMappingTests {
    @Test
    void allBusinessControllersUseApiPrefix() {
        Map<Class<?>, String> expectedMappings = Map.of(
                UserController.class, "/api/user",
                CategoryController.class, "/api/category",
                DessertController.class, "/api/dessert",
                DashboardController.class, "/api/dashboard",
                UploadController.class, "/api/upload",
                OrderController.class, "/api/orders",
                StockRecordController.class, "/api/stock-records"
        );

        expectedMappings.forEach((controller, expected) -> {
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            assertArrayEquals(new String[]{expected}, mapping.value(), controller.getSimpleName());
        });
    }
}

package com.sweet.dessertsystem.order;

import com.sweet.dessertsystem.common.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {
    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<OrderPageResult> page(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.pageForUser(userId(jwt), page, size, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderView> detail(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable Long id) {
        return ApiResponse.ok(orderService.detailForUser(id, userId(jwt)));
    }

    @PostMapping
    public ApiResponse<OrderView> create(@AuthenticationPrincipal Jwt jwt,
                                          @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.createForUser(
                request, userId(jwt), jwt.getClaimAsString("username")));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable Long id) {
        orderService.cancelForUser(id, userId(jwt));
        return ApiResponse.ok();
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}

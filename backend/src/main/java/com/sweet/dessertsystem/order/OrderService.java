package com.sweet.dessertsystem.order;

import com.sweet.dessertsystem.entity.Dessert;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.DessertMapper;
import com.sweet.dessertsystem.stock.StockRecord;
import com.sweet.dessertsystem.stock.StockRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {
    private static final Set<String> STATUSES = Set.of("CREATED", "COMPLETED", "CANCELLED");

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final DessertMapper dessertMapper;
    private final StockRecordMapper stockRecordMapper;

    public OrderService(OrderMapper orderMapper,
                        OrderDetailMapper orderDetailMapper,
                        DessertMapper dessertMapper,
                        StockRecordMapper stockRecordMapper) {
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.dessertMapper = dessertMapper;
        this.stockRecordMapper = stockRecordMapper;
    }

    public OrderPageResult page(long page, long size, String orderNo, String status) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String safeOrderNo = blankToNull(orderNo);
        String safeStatus = normalizeStatus(status);
        long total = orderMapper.countPage(safeOrderNo, safeStatus);
        return new OrderPageResult(
                orderMapper.findPage(safeOrderNo, safeStatus,
                        (safePage - 1) * safeSize, safeSize),
                total, safePage, safeSize);
    }

    public OrderView detail(Long id) {
        OrderView order = orderMapper.findViewById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order.withItems(orderDetailMapper.findViewsByOrderId(id));
    }

    public OrderPageResult pageForUser(Long userId, long page, long size, String status) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String safeStatus = normalizeStatus(status);
        long total = orderMapper.countPageByUserId(userId, safeStatus);
        return new OrderPageResult(
                orderMapper.findPageByUserId(userId, safeStatus,
                        (safePage - 1) * safeSize, safeSize),
                total, safePage, safeSize);
    }

    public OrderView detailForUser(Long id, Long userId) {
        OrderView order = orderMapper.findViewByIdAndUserId(id, userId);
        if (order == null) {
            throw new BusinessException("无权访问该订单");
        }
        return order.withItems(orderDetailMapper.findViewsByOrderId(id));
    }

    @Transactional
    public OrderView create(CreateOrderRequest request) {
        if (request == null || request.customerName() == null
                || request.customerName().trim().isEmpty()) {
            throw new BusinessException("请填写客户名称");
        }
        return createInternal(request, null, request.customerName().trim());
    }

    @Transactional
    public OrderView createForUser(CreateOrderRequest request, Long userId, String username) {
        if (userId == null || username == null || username.trim().isEmpty()) {
            throw new BusinessException("登录信息无效");
        }
        return createInternal(request, userId, username.trim());
    }

    private OrderView createInternal(CreateOrderRequest request,
                                     Long userId,
                                     String customerName) {
        Map<Long, Integer> quantities = validateAndAggregate(request);
        List<Dessert> desserts = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Dessert dessert = dessertMapper.findByIdForUpdate(entry.getKey());
            if (dessert == null) {
                throw new BusinessException("所选甜品不存在");
            }
            if (!Integer.valueOf(1).equals(dessert.getStatus())) {
                throw new BusinessException(dessert.getName() + "已下架");
            }
            if (dessert.getStock() == null || dessert.getStock() < entry.getValue()) {
                throw new BusinessException(dessert.getName() + "库存不足");
            }
            desserts.add(dessert);
            total = total.add(dessert.getPrice()
                    .multiply(BigDecimal.valueOf(entry.getValue())));
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setCustomerName(customerName);
        order.setCustomerPhone(blankToNull(request.customerPhone()));
        order.setRemark(blankToNull(request.remark()));
        order.setStatus("CREATED");
        order.setTotalAmount(total);
        orderMapper.insert(order);

        List<OrderItemView> views = new ArrayList<>();
        for (Dessert dessert : desserts) {
            int quantity = quantities.get(dessert.getId());
            int before = dessert.getStock();
            int after = before - quantity;
            dessert.setStock(after);
            if (dessertMapper.updateById(dessert) != 1) {
                throw new BusinessException("库存更新失败，请重试");
            }

            BigDecimal subtotal = dessert.getPrice().multiply(BigDecimal.valueOf(quantity));
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setDessertId(dessert.getId());
            detail.setDessertName(dessert.getName());
            detail.setUnitPrice(dessert.getPrice());
            detail.setQuantity(quantity);
            detail.setSubtotal(subtotal);
            orderDetailMapper.insert(detail);

            insertStockRecord(dessert.getId(), order.getId(), -quantity,
                    before, after, "ORDER_OUT", "订单 " + order.getOrderNo() + " 扣减");
            views.add(new OrderItemView(detail.getId(), dessert.getId(), dessert.getName(),
                    dessert.getPrice(), quantity, subtotal));
        }

        return new OrderView(order.getId(), order.getOrderNo(), order.getCustomerName(),
                order.getCustomerPhone(), order.getTotalAmount(), order.getStatus(),
                order.getRemark(), order.getCreateTime(), order.getUpdateTime(), views);
    }

    @Transactional
    public void complete(Long id) {
        Order order = requireCreatedOrder(id, "只有已创建订单可以完成");
        order.setStatus("COMPLETED");
        orderMapper.updateById(order);
    }

    @Transactional
    public void cancel(Long id) {
        Order order = requireCreatedOrder(id, "只有已创建订单可以取消");
        cancelOrder(order);
    }

    @Transactional
    public void cancelForUser(Long id, Long userId) {
        Order order = orderMapper.findByIdAndUserIdForUpdate(id, userId);
        if (order == null) {
            throw new BusinessException("无权访问该订单");
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new BusinessException("只有已创建订单可以取消");
        }
        cancelOrder(order);
    }

    private void cancelOrder(Order order) {
        Long id = order.getId();
        for (OrderDetail detail : orderDetailMapper.selectByOrderId(id)) {
            Dessert dessert = dessertMapper.findByIdForUpdate(detail.getDessertId());
            if (dessert == null) {
                throw new BusinessException("订单甜品不存在，无法返还库存");
            }
            int before = dessert.getStock() == null ? 0 : dessert.getStock();
            int after = before + detail.getQuantity();
            dessert.setStock(after);
            if (dessertMapper.updateById(dessert) != 1) {
                throw new BusinessException("库存返还失败，请重试");
            }
            insertStockRecord(dessert.getId(), id, detail.getQuantity(),
                    before, after, "ORDER_RETURN", "取消订单 " + order.getOrderNo() + " 返还");
        }
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
    }

    private Map<Long, Integer> validateAndAggregate(CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("订单至少需要一件甜品");
        }
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : request.items()) {
            if (item == null || item.dessertId() == null
                    || item.quantity() == null || item.quantity() <= 0) {
                throw new BusinessException("订单商品和数量必须有效");
            }
            Integer current = quantities.get(item.dessertId());
            if (current == null) {
                quantities.put(item.dessertId(), item.quantity());
                continue;
            }
            try {
                quantities.put(item.dessertId(), Math.addExact(current, item.quantity()));
            } catch (ArithmeticException exception) {
                throw new BusinessException("甜品数量过大");
            }
        }
        return quantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    private Order requireCreatedOrder(Long id, String message) {
        Order order = orderMapper.findByIdForUpdate(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new BusinessException(message);
        }
        return order;
    }

    private void insertStockRecord(Long dessertId, Long orderId, int change,
                                   int before, int after, String type, String remark) {
        StockRecord record = new StockRecord();
        record.setDessertId(dessertId);
        record.setOrderId(orderId);
        record.setChangeQuantity(change);
        record.setBeforeStock(before);
        record.setAfterStock(after);
        record.setType(type);
        record.setRemark(remark);
        stockRecordMapper.insert(record);
    }

    private String generateOrderNo() {
        return "DS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String normalizeStatus(String status) {
        String value = blankToNull(status);
        if (value == null) {
            return null;
        }
        value = value.toUpperCase();
        if (!STATUSES.contains(value)) {
            throw new BusinessException("订单状态无效");
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}

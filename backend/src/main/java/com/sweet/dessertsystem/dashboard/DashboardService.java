package com.sweet.dessertsystem.dashboard;

import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final DashboardMapper mapper;

    public DashboardService(DashboardMapper mapper) {
        this.mapper = mapper;
    }

    public DashboardSummary summary() {
        return new DashboardSummary(
                mapper.countDesserts(),
                mapper.countCategories(),
                mapper.sumStock(),
                mapper.countLowStock(LOW_STOCK_THRESHOLD),
                mapper.findLowStock(LOW_STOCK_THRESHOLD, 5)
        );
    }
}

package com.sweet.dessertsystem.dto;

import java.util.List;

public record DessertPageResult(List<DessertView> records, long total, long page, long size) {
}

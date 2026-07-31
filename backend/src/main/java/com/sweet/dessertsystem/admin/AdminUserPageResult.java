package com.sweet.dessertsystem.admin;

import com.sweet.dessertsystem.dto.UserView;

import java.util.List;

public record AdminUserPageResult(
        List<UserView> records,
        long total,
        long page,
        long size) {
}

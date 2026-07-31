package com.sweet.dessertsystem.dto;

import com.sweet.dessertsystem.entity.User;

import java.time.LocalDateTime;

public record UserView(Long id, String username, String role,
                       Integer status, LocalDateTime createTime) {

    public static UserView from(User user) {
        return new UserView(user.getId(), user.getUsername(), user.getRole(),
                user.getStatus(), user.getCreateTime());
    }
}

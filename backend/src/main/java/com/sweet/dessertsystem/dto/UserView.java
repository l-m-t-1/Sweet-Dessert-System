package com.sweet.dessertsystem.dto;

import com.sweet.dessertsystem.entity.User;

public record UserView(Long id, String username, String role) {

    public static UserView from(User user) {
        return new UserView(user.getId(), user.getUsername(), user.getRole());
    }
}

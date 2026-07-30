package com.sweet.dessertsystem.auth;

public record AuthResponse(String token, Long id, String username, String role) {
}

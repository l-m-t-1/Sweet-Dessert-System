package com.sweet.dessertsystem.common;

public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "");
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, "");
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}

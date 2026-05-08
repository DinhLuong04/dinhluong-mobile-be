package com.dinhluong.dlmstore.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private Date timestamp;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", 200, message, new Date(), data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>("error", code, message, new Date(), null);
    }
}

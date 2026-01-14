package com.dinhluong.dlmstore.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final String code;       // mã lỗi chuẩn
    private final HttpStatus status; // HTTP status code

    public AppException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

package com.dinhluong.dlmstore.exception;

import org.springframework.http.HttpStatus;

public class DataConstraintException extends AppException {
    public DataConstraintException(String message) {
        // Trả về mã lỗi tùy chỉnh "DATA_CONSTRAINT_ERROR" và HTTP Status 409
        super("DATA_CONSTRAINT_ERROR", message, HttpStatus.CONFLICT);
    }
}

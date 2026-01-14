package com.dinhluong.dlmstore.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends AppException {
    public InsufficientStockException(String menuItemName, int available, int requested) {
        super("INSUFFICIENT_STOCK",
                "Insufficient stock for " + menuItemName + ". Available: " + available + ", requested: " + requested,
                HttpStatus.BAD_REQUEST);
    }
}

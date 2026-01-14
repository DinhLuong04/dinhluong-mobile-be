package com.dinhluong.dlmstore.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends AppException {
    public AuthorizationException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }
}

package com.dinhluong.dlmstore.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resourceName, String id) {
        super("RESOURCE_NOT_FOUND",
                resourceName + " not found with id: " + id,
                HttpStatus.NOT_FOUND);
    }
}

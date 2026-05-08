package com.dinhluong.dlmstore.exception;

import com.dinhluong.dlmstore.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationExceptions(RuntimeException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Email hoặc mật khẩu không đúng");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLockingFailureException(
            ObjectOptimisticLockingFailureException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
                "error",
                HttpStatus.CONFLICT.value(),
                "Sản phẩm bạn chọn vừa có người khác nhanh tay mua mất. Vui lòng tải lại giỏ hàng!",
                new Date(),
                null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
                ex.getCode(),
                ex.getStatus().value(),
                ex.getMessage(),
                new Date(),
                null);
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOtherExceptions(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>(
                "error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                new Date(),
                null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
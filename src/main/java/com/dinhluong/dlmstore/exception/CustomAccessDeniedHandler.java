package com.dinhluong.dlmstore.exception;

import com.dinhluong.dlmstore.dto.ApiResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        AuthorizationException ex = new AuthorizationException("Bạn không có quyền truy cập");

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getStatus().value(),
                ex.getMessage()
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(apiResponse)
        );
    }
}

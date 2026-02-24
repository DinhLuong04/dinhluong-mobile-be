package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;

import lombok.Data;

@Builder
@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String authProvider;
    private String roleName;
    private Boolean isEnabled;
    private java.time.LocalDateTime createdAt;

}

package com.dinhluong.dlmstore.dto.requests;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {
    private String email;
    private String password;
}

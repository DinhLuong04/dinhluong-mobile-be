package com.dinhluong.dlmstore.dto.responses;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Long id;
    private String email;
    private String name;
    private String avatar;
    private String TypeAccount;
}

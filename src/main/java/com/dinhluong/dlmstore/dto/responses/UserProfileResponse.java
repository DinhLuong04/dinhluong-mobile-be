package com.dinhluong.dlmstore.dto.responses;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
}

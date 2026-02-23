package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.UserProfileStatsResponse;
import com.dinhluong.dlmstore.security.CustomUserPrincipal; // Nhớ import class này
import com.dinhluong.dlmstore.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import annotation của Spring Security
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

   @GetMapping("/profile-stats")
    public ResponseEntity<ApiResponse<UserProfileStatsResponse>> getProfileStats(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        Long userId = currentUser.getId(); 
        UserProfileStatsResponse stats = userProfileService.getUserStats(userId);
        
        // Bọc dữ liệu vào ApiResponse.success
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tổng quan thành công", stats));
    }
}
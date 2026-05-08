package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.ChangePasswordRequest;
import com.dinhluong.dlmstore.dto.responses.UserProfileResponse;
import com.dinhluong.dlmstore.dto.responses.UserProfileStatsResponse;
import com.dinhluong.dlmstore.security.CustomUserPrincipal;
import com.dinhluong.dlmstore.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tổng quan thành công", stats));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        UserProfileResponse profile = userProfileService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân thành công", profile));
    }

    @PutMapping(value = "/profile", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        UserProfileResponse updatedProfile = userProfileService.updateProfile(currentUser.getId(), fullName, phone,
                avatar);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", updatedProfile));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            @RequestBody ChangePasswordRequest request) {

        userProfileService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Thay đổi mật khẩu thành công", null));
    }
}
package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.UserDetailResponse;
import com.dinhluong.dlmstore.dto.responses.UserResponse;
import com.dinhluong.dlmstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // Lấy danh sách người dùng
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isEnabled) {

        List<UserResponse> users = userService.getAdminUsers(keyword, isEnabled);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", users));
    }

    // Lấy chi tiết 1 người dùng (Bao gồm địa chỉ, đơn hàng, thống kê)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        try {
            UserDetailResponse userDetail = userService.getUserDetail(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết người dùng thành công", userDetail));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // Khóa / Mở khóa tài khoản
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(@PathVariable Long id) {
        try {
            userService.toggleUserStatus(id);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
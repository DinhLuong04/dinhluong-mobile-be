package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.UserDashboardStats;
import com.dinhluong.dlmstore.dto.responses.UserDetailResponse;
import com.dinhluong.dlmstore.dto.responses.UserResponse;

import com.dinhluong.dlmstore.service.UserService;
import com.dinhluong.dlmstore.service.impl.Excels.UserExcelExportServiceImpl;
import com.dinhluong.dlmstore.service.impl.Excels.UserExcelImportServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.springframework.http.HttpHeaders;
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
   private final UserExcelExportServiceImpl userExportService; 
   private final UserExcelImportServiceImpl userImportService;
    // Lấy danh sách người dùng
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isEnabled) {

        List<UserResponse> users = userService.getAdminUsers(keyword, isEnabled);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", users));
    }
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserDashboardStats>> getUserStats() {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", userService.getUserStatistics()));
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

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportUsersExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isEnabled) {
        
        // 1. Chỉ gọi 1 hàm duy nhất từ Service
        ByteArrayInputStream in = userExportService.generateExportData(keyword, isEnabled);

        // 2. Setup Header HTTP báo cho Browser tải file (Logic của HTTP bắt buộc nằm ở Controller)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=DanhSachKhachHang.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> importUsersExcel(@RequestParam("file") MultipartFile file) {
        
        // Kiểm tra định dạng file tại Controller để tránh xử lý thừa
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Vui lòng chọn file Excel đúng định dạng (.xlsx)"));
        }

        try {
            // Gọi logic xử lý chính trong Service
            String resultMsg = userImportService.importFromExcel(file);
            return ResponseEntity.ok(ApiResponse.success(resultMsg, null));
        } catch (Exception e) {
            // Trả về lỗi nếu quá trình đọc/ghi database gặp vấn đề
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Lỗi khi nhập dữ liệu: " + e.getMessage()));
        }
    }
}
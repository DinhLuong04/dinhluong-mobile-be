package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.ImportUserReport;
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

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ImportUserReport>> importUsersExcel(@RequestParam("file") MultipartFile file) {
        try {
            // Kiểm tra file rỗng trước khi đẩy xuống Service
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "File tải lên không được để trống"));
            }

            // Gọi Service để xử lý Import và lấy báo cáo
            ImportUserReport report = userImportService.importFromExcel(file);

            // Tùy chỉnh câu thông báo dựa trên kết quả
            String message = "Xử lý file hoàn tất. Thành công: " + report.getSuccessCount() + ", Thất bại: " + report.getFailCount();

            return ResponseEntity.ok(ApiResponse.success(message, report));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Lỗi khi import: " + e.getMessage()));
        }
    }
}
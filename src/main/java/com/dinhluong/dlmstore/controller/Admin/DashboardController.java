package com.dinhluong.dlmstore.controller.Admin;


import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.DashboardResponse;
import com.dinhluong.dlmstore.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(defaultValue = "this_month", required = false) String time) {
        try {
            DashboardResponse data = dashboardService.getDashboardData(time);
            return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu dashboard thành công", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Lỗi khi lấy dữ liệu dashboard: " + e.getMessage()));
        }
    }
}
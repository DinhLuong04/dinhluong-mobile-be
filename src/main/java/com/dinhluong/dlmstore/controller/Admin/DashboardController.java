package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.DashboardResponse;
import com.dinhluong.dlmstore.service.DashboardService;
import com.dinhluong.dlmstore.service.ExcelExportService; // Bổ sung import interface Excel
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;

// Bổ sung các import cần thiết để xử lý trả về file
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    
    // BỔ SUNG KHAI BÁO NÀY ĐỂ SPRING BOOT INJECT LỚP XỬ LÝ EXCEL
    private final ExcelExportService<DashboardResponse> dashboardExcelService;

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

    @GetMapping("/export")
    public ResponseEntity<Resource> exportDashboard(@RequestParam(required = false, defaultValue = "this_month") String time) {
        
        // 1. Lấy Data từ Database (Tách biệt hoàn toàn DB và Excel)
        DashboardResponse data = dashboardService.getDashboardData(time);

        // 2. Đưa Data vào lớp con xử lý xuất Excel
        ByteArrayInputStream in = dashboardExcelService.exportToExcel(data, time);

        // 3. Trả file về cho Frontend
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bao_cao_dashboard_" + time + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
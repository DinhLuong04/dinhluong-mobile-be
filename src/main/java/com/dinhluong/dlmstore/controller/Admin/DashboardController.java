package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.AiBusinessInsightResponse;
import com.dinhluong.dlmstore.dto.responses.DashboardResponse;
import com.dinhluong.dlmstore.service.DashboardService;
import com.dinhluong.dlmstore.service.ExcelExportService;
import com.dinhluong.dlmstore.service.tools.GeminiAiService;

import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;

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
    private final GeminiAiService geminiAiService;
    private final ExcelExportService<DashboardResponse> dashboardExcelService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(defaultValue = "this_month", required = false) String time,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // Truyền thêm startDate và endDate xuống Service
            DashboardResponse data = dashboardService.getDashboardData(time, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu dashboard thành công", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Lỗi khi lấy dữ liệu dashboard: " + e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportDashboard(
            @RequestParam(required = false, defaultValue = "this_month") String time,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // 1. Lấy Data từ Database có hỗ trợ Custom Date
        DashboardResponse data = dashboardService.getDashboardData(time, startDate, endDate);

        // 2. Đưa Data vào lớp con xử lý xuất Excel
        ByteArrayInputStream in = dashboardExcelService.exportToExcel(data, time);

        // 3. Trả file về cho Frontend
        HttpHeaders headers = new HttpHeaders();
        // Cập nhật tên file khi chọn custom
        String fileNameSuffix = time.equals("custom") ? (startDate + "_đến_" + endDate) : time;
        headers.add("Content-Disposition", "attachment; filename=bao_cao_dashboard_" + fileNameSuffix + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/ai-insights")
    public ResponseEntity<ApiResponse<?>> getAiInsights(
            @RequestParam(defaultValue = "this_month", required = false) String time,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 1. Lấy data hệt như cách lấy dashboard
            DashboardResponse data = dashboardService.getDashboardData(time, startDate, endDate);
            
            // 2. Đưa cho AI phân tích
            AiBusinessInsightResponse aiAnalysisResult = geminiAiService.analyzeDashboardData(data);
            
            return ResponseEntity.ok(ApiResponse.success("Phân tích AI thành công", aiAnalysisResult));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Lỗi phân tích AI: " + e.getMessage()));
        }
    }
}
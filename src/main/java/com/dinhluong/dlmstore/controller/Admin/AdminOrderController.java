package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.BulkStatusRequest;
import com.dinhluong.dlmstore.dto.responses.OrderResponse;
import com.dinhluong.dlmstore.dto.responses.OrderStatsResponse;
import com.dinhluong.dlmstore.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    
    // ==========================================
    // 1. LẤY DANH SÁCH ĐƠN HÀNG
    // ==========================================
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword) {

        List<OrderResponse> orders = orderService.getAdminOrders(status, keyword);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách thành công", orders)
        );
    }

    // ==========================================
    // 2. CẬP NHẬT TRẠNG THÁI 1 ĐƠN
    // ==========================================
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {

            String newStatus = request.get("status");
            String reason = request.get("reason");

            OrderResponse updatedOrder =
                    orderService.updateOrderStatus(
                            id,
                            newStatus,
                            reason,
                            "ADMIN"
                    );

            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật thành công", updatedOrder)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(400, e.getMessage())
            );
        }
    }

    // ==========================================
    // 3. THỐNG KÊ
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> getStats() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thống kê thành công",
                        orderService.getOrderStatistics()
                )
        );
    }

    // ==========================================
    // 4. CẬP NHẬT HÀNG LOẠT
    // ==========================================
    @PutMapping("/bulk-status")
    public ResponseEntity<ApiResponse<String>> updateBulkStatus(
            @RequestBody BulkStatusRequest request) {

        try {

            orderService.updateStatusBatch(
                    request.getOrderIds(),
                    request.getNewStatus(),
                    request.getReason(),
                    "ADMIN"
            );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Đã cập nhật " + request.getOrderIds().size() + " đơn hàng",
                            null
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                            400,
                            "Lỗi cập nhật hàng loạt: " + e.getMessage()
                    )
            );
        }
    }

    // ==========================================
    // 5. XUẤT EXCEL
    // ==========================================
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword) {

        ByteArrayInputStream excelFile =
                orderService.exportOrdersExcel(status, keyword);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=orders.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(new InputStreamResource(excelFile));
    }
}
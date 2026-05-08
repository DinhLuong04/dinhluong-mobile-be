package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.OrderResponse;
import com.dinhluong.dlmstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // 1. Lấy danh sách có Filter & Search
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword) {

        List<OrderResponse> orders = orderService.getAdminOrders(status, keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", orders));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {
            String newStatus = request.get("status");
            String reason = request.get("reason");
            OrderResponse updatedOrder = orderService.updateOrderStatus(id, newStatus, reason, "ADMIN");
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}

package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.responses.PaymentResponse;
import com.dinhluong.dlmstore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "ALL") String method,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword) {

        List<PaymentResponse> payments = paymentService.getAdminPayments(method, status, keyword);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giao dịch thành công", payments));
    }

    // ==========================================
    // 🔥 THÊM API XÁC NHẬN ĐÃ HOÀN TIỀN
    // ==========================================
    @PutMapping("/{id}/refunded")
    public ResponseEntity<ApiResponse<String>> confirmRefund(@PathVariable Long id) {

        // Gọi logic xử lý từ Service (Cập nhật trạng thái + Gửi Noti)
        paymentService.confirmRefund(id);

        // Trả về response theo chuẩn ApiResponse hiện có của bạn
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái hoàn tiền thành công", null));
    }
}
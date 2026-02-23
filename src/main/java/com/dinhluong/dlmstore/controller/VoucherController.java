package com.dinhluong.dlmstore.controller;



import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
// @CrossOrigin("*") // Mở comment này nếu bạn bị lỗi CORS và chưa config CORS tổng
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableVouchers(@RequestParam BigDecimal totalAmount) {
        List<Voucher> vouchers = voucherService.getAvailableVouchers(totalAmount);
        
        // Build response khớp với format { data: [...] } mà React mong đợi
        Map<String, Object> response = new HashMap<>();
        response.put("data", vouchers);
        response.put("message", "Lấy danh sách voucher thành công");
        response.put("status", 200);
        
        return ResponseEntity.ok(response);
    }
}

package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Voucher>>> getAllVouchers(
            @RequestParam(required = false) String keyword) {
        List<Voucher> vouchers = voucherService.getAdminVouchers(keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", vouchers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Voucher>> createVoucher(@RequestBody Voucher voucher) {
        try {
            Voucher created = voucherService.createVoucher(voucher);
            return ResponseEntity.ok(ApiResponse.success("Thêm mã giảm giá thành công", created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Voucher>> updateVoucher(
            @PathVariable Long id, @RequestBody Voucher voucher) {
        try {
            Voucher updated = voucherService.updateVoucher(id, voucher);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật mã giảm giá thành công", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteVoucher(@PathVariable Long id) {
        try {
            voucherService.deleteVoucher(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Không thể xóa mã giảm giá này. Có thể đã được người dùng thu thập."));
        }
    }
}

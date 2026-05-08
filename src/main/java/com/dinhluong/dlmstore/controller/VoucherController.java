package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.entity.UserVoucher;
import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.service.VoucherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dinhluong.dlmstore.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Voucher>>> getAvailableVouchers(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        List<Voucher> vouchers = voucherService.getAvailableVouchers(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách voucher khả dụng thành công", vouchers));
    }

    @GetMapping("/my-vouchers")
    public ResponseEntity<ApiResponse<List<UserVoucher>>> getMyVouchers(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        List<UserVoucher> myVouchers = voucherService.getMyVouchers(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách voucher của bạn thành công", myVouchers));
    }

    @PostMapping("/{voucherId}/collect")
    public ResponseEntity<ApiResponse<String>> collectVoucher(
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            @PathVariable Long voucherId) {

        String message = voucherService.collectVoucher(currentUser.getId(), voucherId);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}
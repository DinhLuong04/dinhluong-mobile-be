package com.dinhluong.dlmstore.service;



import com.dinhluong.dlmstore.entity.UserVoucher;
import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.repository.UserVoucherRepository;
import com.dinhluong.dlmstore.repository.VoucherRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

   private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    // Hiển thị các voucher đang phát hành (còn hạn, còn lượt)
    public List<Voucher> getAvailableVouchers(Long userId) {
        return voucherRepository.findAvailableVouchersForUser(userId);
    }

    // Lấy danh sách voucher mà user ĐÃ thu thập vào ví
    public List<UserVoucher> getMyVouchers(Long userId) {
        return userVoucherRepository.findByUserId(userId);
    }

    // User bấm nút "Thu thập" voucher
    @Transactional
    public String collectVoucher(Long userId, Long voucherId) {
        // 1. Kiểm tra xem user đã lấy voucher này chưa (mỗi user chỉ lấy 1 lần)
        if (userVoucherRepository.existsByUserIdAndVoucherId(userId, voucherId)) {
            return "Bạn đã thu thập voucher này rồi!";
        }

        // 2. Lấy thông tin voucher từ DB
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // 3. Kiểm tra tính hợp lệ của voucher (hạn sử dụng, số lượng)
        if (!voucher.isCollectable()) {
        return "Voucher đã hết hạn hoặc đã được thu thập hết!";
    }

        // 4. Lưu vào ví voucher của user
        UserVoucher userVoucher = UserVoucher.builder()
            .userId(userId)
            .voucher(voucher)
            .isUsed(false)
            .build();
            
        userVoucherRepository.save(userVoucher);
        
        return "Thu thập voucher thành công!";
    }
}

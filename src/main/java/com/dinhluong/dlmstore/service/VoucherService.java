package com.dinhluong.dlmstore.service;



import com.dinhluong.dlmstore.entity.Voucher;
import com.dinhluong.dlmstore.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public List<Voucher> getAvailableVouchers(BigDecimal totalAmount) {
        // Có thể thêm các logic nghiệp vụ khác ở đây nếu cần (ví dụ: check hạng thành viên)
        return voucherRepository.findAvailableVouchers(totalAmount);
    }
}

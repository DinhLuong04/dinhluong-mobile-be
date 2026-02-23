package com.dinhluong.dlmstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.UserVoucher;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
   // Lấy danh sách ví voucher của user
    List<UserVoucher> findByUserId(Long userId);
    
    // Tìm cụ thể 1 voucher của 1 user
    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);
    
    // Kiểm tra xem đã lưu chưa
    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);
}
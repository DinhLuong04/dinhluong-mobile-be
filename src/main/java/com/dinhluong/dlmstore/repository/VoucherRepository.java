package com.dinhluong.dlmstore.repository;


import com.dinhluong.dlmstore.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Query("SELECT v FROM Voucher v " +
           "WHERE v.expiryDate > CURRENT_TIMESTAMP " +
           "AND v.usedCount < v.usageLimit " +
           "AND v.minOrderAmount <= :totalAmount")
    List<Voucher> findAvailableVouchers(@Param("totalAmount") BigDecimal totalAmount);
    
    // Thêm hàm này để sau này check lúc submit Order
    Voucher findByCode(String code);
}
package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    // Cấu hình Query cho bộ lọc của Admin
    @Query("SELECT p FROM Payment p WHERE " +
           "(:method IS NULL OR p.method = :method) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(p.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(p.orderId AS string) LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.id DESC")
    List<Payment> searchAdminPayments(@Param("method") Payment.PaymentMethod method,
                                      @Param("status") Payment.PaymentStatus status,
                                      @Param("keyword") String keyword);
}
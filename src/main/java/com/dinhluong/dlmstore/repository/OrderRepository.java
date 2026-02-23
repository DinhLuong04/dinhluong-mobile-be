package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Hàm này để sau này làm API: Lấy danh sách đơn hàng của 1 user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);


    
    // Lấy đơn hàng theo trạng thái
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Order.OrderStatus status);
    
    // Lấy 5 đơn hàng gần nhất (Cho màn Overview)
    List<Order> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    // Tính tổng tiền các đơn hàng ĐÃ GIAO THÀNH CÔNG (để tính hạng thành viên)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.status = 'DELIVERED'")
    BigDecimal getTotalSpentByUserId(@Param("userId") Long userId);

    // Đếm số lượng đơn hàng thành công
    Integer countByUserIdAndStatus(Long userId, Order.OrderStatus status);
}
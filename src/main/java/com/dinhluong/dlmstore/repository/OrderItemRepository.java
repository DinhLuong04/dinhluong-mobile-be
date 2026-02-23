package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Lấy TẤT CẢ sản phẩm trong 1 đơn hàng
    List<OrderItem> findByOrderId(Long orderId);

    
   
   
}

package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Lấy thông báo của User, sắp xếp mới nhất lên đầu
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Đếm số thông báo chưa đọc
    long countByUserIdAndIsReadFalse(Long userId);
}
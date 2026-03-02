package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.entity.Notification;
import com.dinhluong.dlmstore.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Hàm gọi để lấy danh sách
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    public void markAsRead(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
    
    notification.setRead(true);
    notificationRepository.save(notification);
}
    // Hàm đánh dấu đã đọc
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // 🔥 HÀM QUAN TRỌNG: Lưu DB và Bắn WebSocket
    @Transactional
    public void createAndSendNotification(Long userId, Notification.NotificationType type, String message) {
        // 1. Lưu vào Database
        Notification notif = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .build();
        Notification savedNotif = notificationRepository.save(notif);

        // 2. Bắn Real-time tới kênh của Client
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications", // <-- Kênh lắng nghe
                savedNotif
        );
    }
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
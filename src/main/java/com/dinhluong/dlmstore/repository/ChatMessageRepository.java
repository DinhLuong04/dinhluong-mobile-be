package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. Lấy lịch sử chat giữa 2 người (đã có)
    @Query("SELECT c FROM ChatMessage c WHERE (c.senderId = :user1 AND c.receiverId = :user2) OR (c.senderId = :user2 AND c.receiverId = :user1) ORDER BY c.sentAt ASC")
    List<ChatMessage> findConversation(@Param("user1") Long user1, @Param("user2") Long user2);

    // 2. Lấy danh sách tin nhắn MỚI NHẤT của từng hội thoại đối với 1 user (dùng cho Admin)
    @Query(value = "SELECT m1.* FROM chat_messages m1 " +
                   "INNER JOIN (" +
                   "    SELECT " +
                   "        CASE WHEN sender_id = :userId THEN receiver_id ELSE sender_id END AS other_user, " +
                   "        MAX(sent_at) AS max_sent_at " +
                   "    FROM chat_messages " +
                   "    WHERE sender_id = :userId OR receiver_id = :userId " +
                   "    GROUP BY CASE WHEN sender_id = :userId THEN receiver_id ELSE sender_id END" +
                   ") m2 " +
                   "ON (CASE WHEN m1.sender_id = :userId THEN m1.receiver_id ELSE m1.sender_id END) = m2.other_user " +
                   "AND m1.sent_at = m2.max_sent_at " +
                   "ORDER BY m1.sent_at DESC", nativeQuery = true)
    List<ChatMessage> findRecentConversationsForUser(@Param("userId") Long userId);

    // 3. Đếm số tin nhắn chưa đọc mà người khác gửi cho mình
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.senderId = :senderId AND c.receiverId = :receiverId AND c.isRead = false")
    Long countUnreadMessages(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // 4. Đánh dấu đã đọc tất cả tin nhắn từ một người
    @Modifying
    @Query("UPDATE ChatMessage c SET c.isRead = true WHERE c.senderId = :senderId AND c.receiverId = :receiverId AND c.isRead = false")
    void markMessagesAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);


    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.isRead = false")
long countUnreadMessages();
}
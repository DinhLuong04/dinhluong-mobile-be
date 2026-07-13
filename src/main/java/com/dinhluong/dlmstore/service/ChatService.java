package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.ConversationDTO;
import com.dinhluong.dlmstore.entity.ChatMessage;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.ChatMessageRepository;
import com.dinhluong.dlmstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository; // Tiêm thêm để lấy thông tin User

    // Lấy danh sách hội thoại cho Admin
    public List<ConversationDTO> getConversations(Long adminId) {
        List<ChatMessage> recentMessages = chatMessageRepository.findRecentConversationsForUser(adminId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for (ChatMessage msg : recentMessages) {
            Long otherUserId = msg.getSenderId().equals(adminId) ? msg.getReceiverId() : msg.getSenderId();
            Long unreadCount = chatMessageRepository.countUnreadMessages(otherUserId, adminId);
            
            // Lấy thông tin user để hiển thị lên UI
            String name = "Khách hàng ẩn danh";
            String avatar = null;
            Users user = userRepository.findById(otherUserId).orElse(null);
            if (user != null) {
                name = user.getFullName() != null ? user.getFullName() : user.getEmail();
                avatar = user.getAvatarUrl();
            }

            ConversationDTO dto = ConversationDTO.builder()
                    .userId(otherUserId)
                    .userName(name)
                    .userAvatar(avatar)
                    .lastMessage(msg.getMessage())
                    .sentAt(msg.getSentAt() != null ? msg.getSentAt().toString() : null)
                    .unreadCount(unreadCount)
                    .isRead(msg.getIsRead())
                    .build();
            
            conversations.add(dto);
        }
        return conversations;
    }

    // THÊM MỚI: Lấy chi tiết đoạn chat giữa Admin và 1 User
    public List<ChatMessage> getConversationHistory(Long adminId, Long userId) {
        return chatMessageRepository.findConversation(adminId, userId);
    }

    // THÊM MỚI: Gửi tin nhắn
    @Transactional
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Nội dung tin nhắn không được để trống");
        }

        // Giới hạn độ dài để tránh spam phá hoại DB
        if (content.length() > 2000) {
            throw new RuntimeException("Tin nhắn quá dài");
        }
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .message(content)
                .isRead(false)
                .build();
        return chatMessageRepository.save(message);
    }

    // Cập nhật trạng thái "Đã đọc"
    @Transactional
    public void markAsRead(Long senderId, Long receiverId) {
        chatMessageRepository.markMessagesAsRead(senderId, receiverId);
    }
}
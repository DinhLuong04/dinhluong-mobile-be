package com.dinhluong.dlmstore.service;



import com.dinhluong.dlmstore.dto.ConversationDTO;
import com.dinhluong.dlmstore.entity.ChatMessage;
import com.dinhluong.dlmstore.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // Lấy danh sách hội thoại cho Admin
    public List<ConversationDTO> getConversations(Long adminId) {
        // Lấy tin nhắn cuối của mỗi đoạn hội thoại
        List<ChatMessage> recentMessages = chatMessageRepository.findRecentConversationsForUser(adminId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for (ChatMessage msg : recentMessages) {
            // Xác định ai là người đang chat với Admin
            Long otherUserId = msg.getSenderId().equals(adminId) ? msg.getReceiverId() : msg.getSenderId();
            
            // Đếm số tin nhắn chưa đọc mà khách hàng đó gửi cho Admin
            Long unreadCount = chatMessageRepository.countUnreadMessages(otherUserId, adminId);

            ConversationDTO dto = ConversationDTO.builder()
                    .userId(otherUserId)
                    .lastMessage(msg.getMessage())
                    .sentAt(msg.getSentAt().toString())
                    .unreadCount(unreadCount)
                    .isRead(msg.getIsRead())
                    .build();
            
            conversations.add(dto);
        }
        return conversations;
    }

    // Cập nhật trạng thái "Đã đọc"
    @Transactional
    public void markAsRead(Long senderId, Long receiverId) {
        chatMessageRepository.markMessagesAsRead(senderId, receiverId);
    }
}

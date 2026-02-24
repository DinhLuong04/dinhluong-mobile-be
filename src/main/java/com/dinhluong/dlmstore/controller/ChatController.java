package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ChatMessageDTO;
import com.dinhluong.dlmstore.dto.ConversationDTO;
import com.dinhluong.dlmstore.entity.ChatMessage;
import com.dinhluong.dlmstore.repository.ChatMessageRepository;
import com.dinhluong.dlmstore.security.CustomUserPrincipal;
import com.dinhluong.dlmstore.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;
   @Autowired
    private ChatService chatService;
    // 1. XỬ LÝ WEBSOCKET: Nhận tin nhắn và gửi đi
    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // Lưu vào DB
        ChatMessage savedMsg = chatMessageRepository.save(ChatMessage.builder()
                .senderId(chatMessageDTO.getSenderId())
                .receiverId(chatMessageDTO.getReceiverId())
                .message(chatMessageDTO.getMessage())
                .isRead(false)
                .build());

        ChatMessageDTO responseMsg = ChatMessageDTO.builder()
                .id(savedMsg.getId())
                .senderId(savedMsg.getSenderId())
                .receiverId(savedMsg.getReceiverId())
                .message(savedMsg.getMessage())
                .sentAt(savedMsg.getSentAt().toString())
                .build();

        // Gửi tin nhắn đến hàng đợi riêng của người nhận (Receiver)
        // Kênh đích sẽ là: /user/{receiverId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatMessageDTO.getReceiverId()),
                "/queue/messages",
                responseMsg
        );
    }

    // 2. REST API: Lấy lịch sử chat
    @GetMapping("/api/chat/history/{adminId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable Long adminId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        Long currentUserId = currentUser.getId();
        
        List<ChatMessage> messages = chatMessageRepository.findConversation(currentUserId, adminId);
        
        List<ChatMessageDTO> dtos = messages.stream().map(msg -> ChatMessageDTO.builder()
                .id(msg.getId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .message(msg.getMessage())
                .sentAt(msg.getSentAt().toString())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 3. (MỚI) API cho Admin lấy danh sách các đoạn hội thoại
    @GetMapping("/api/chat/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(@AuthenticationPrincipal CustomUserPrincipal currentUser) {
        List<ConversationDTO> conversations = chatService.getConversations(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    // 4. (MỚI) API đánh dấu tin nhắn đã đọc (dùng khi khách đang mở tab chat)
    @PutMapping("/api/chat/read/{senderId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        chatService.markAsRead(senderId, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.ChatMessageDTO;
import com.dinhluong.dlmstore.dto.ConversationDTO;
import com.dinhluong.dlmstore.entity.ChatMessage;
import com.dinhluong.dlmstore.service.ChatService;
import com.dinhluong.dlmstore.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getConversations(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        Long adminId = currentUser.getId();
        List<ConversationDTO> conversations = chatService.getConversations(adminId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hội thoại thành công", conversations));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getHistory(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        Long adminId = currentUser.getId();
        chatService.markAsRead(userId, adminId);

        List<ChatMessage> history = chatService.getConversationHistory(adminId, userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử chat thành công", history));
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        Long adminId = currentUser.getId();
        String content = payload.get("message");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Tin nhắn không được để trống"));
        }

        ChatMessage savedMessage = chatService.sendMessage(adminId, userId, content);

        ChatMessageDTO responseMsg = ChatMessageDTO.builder()
                .id(savedMessage.getId())
                .senderId(savedMessage.getSenderId())
                .receiverId(savedMessage.getReceiverId())
                .message(savedMessage.getMessage())
                .sentAt(savedMessage.getSentAt() != null ? savedMessage.getSentAt().toString() : null)
                .build();

        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/messages",
                responseMsg);

        return ResponseEntity.ok(ApiResponse.success("Gửi tin nhắn thành công", savedMessage));
    }
}
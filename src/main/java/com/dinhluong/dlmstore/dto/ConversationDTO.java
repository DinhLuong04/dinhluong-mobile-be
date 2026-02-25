package com.dinhluong.dlmstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long userId; 
    private String userName;   // Thêm để hiển thị UI
    private String userAvatar; // Thêm để hiển thị UI        // ID của khách hàng đang chat với Admin
    private String lastMessage;   // Nội dung tin nhắn cuối
    private String sentAt;        // Thời gian gửi tin cuối
    private Long unreadCount;     // Số tin nhắn chưa đọc
    private Boolean isRead;       // Trạng thái của tin nhắn cuối
    // Nếu bạn có Entity User, bạn có thể bổ sung thêm: private String userName;
}
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
    private String userName;
    private String userAvatar;
    private String lastMessage;
    private String sentAt;
    private Long unreadCount;
    private Boolean isRead;

}
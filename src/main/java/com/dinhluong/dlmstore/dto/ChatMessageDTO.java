package com.dinhluong.dlmstore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String message;
    private String sentAt;
}
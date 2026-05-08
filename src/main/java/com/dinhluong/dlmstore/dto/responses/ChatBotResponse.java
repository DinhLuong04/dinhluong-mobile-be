package com.dinhluong.dlmstore.dto.responses;

import java.util.List;

import com.dinhluong.dlmstore.dto.ChatProductDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ChatBotResponse {
    private String answer;
    private List<ChatProductDto> products;
}

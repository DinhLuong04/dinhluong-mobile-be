package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;

@Data
public class AiSpecExtractRequest {
    private String rawText;        // Đoạn văn bản lộn xộn admin copy trên mạng
    private String attributesInfo; // Danh sách "ID - Tên thuộc tính" gửi từ React
}
package com.dinhluong.dlmstore.dto.requests;

import java.util.List;

import lombok.Data;

@Data
public class AiAccessoryRequest {
    private String productName;
    private String specificationsJson; // Chính là mảng JSON bạn đã bóc tách
    private List<String> imageUrls;
    private String rawText;
}
package com.dinhluong.dlmstore.dto.requests;

import java.util.List;

import lombok.Data;

@Data
public class AiContentRequest {
    private String productName;
    private String specificationsJson;
    private List<String> imageUrls; // Danh sách các URL ảnh hiện có (Thumbnail + Gallery)
}

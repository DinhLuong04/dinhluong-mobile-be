package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;

@Data
public class SpecRequest {
    // Dùng chung
    private String name;
    private Integer sortOrder;
    
    // Dành riêng cho SpecAttribute
    private Long groupId;
    private String dataType; // TEXT, NUMBER, BOOLEAN, SELECT
}
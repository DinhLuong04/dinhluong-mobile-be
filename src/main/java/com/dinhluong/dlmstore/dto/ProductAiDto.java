package com.dinhluong.dlmstore.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductAiDto {
    private Long id;
    private String name;
    private String slug;         // Để tạo link
    private BigDecimal price;    // Giá hiển thị
    
    // Tóm tắt kho hàng: "Đỏ-128GB: Còn 5; Xanh-256GB: Hết"
    private List<String> inventory; 
    
    // Thông số kỹ thuật dạng Map phẳng: {"Chip": "Snapdragon", "Pin": "5000mAh"}
    private Map<String, String> specs; 
    
    private String promotion;    // "Giảm 300k, Trả góp 0%"
}

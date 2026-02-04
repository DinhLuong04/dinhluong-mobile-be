package com.dinhluong.dlmstore.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ChatProductDto {
    private Long id;
    private String name;
    private String slug;            // Để click vào xem chi tiết
    private String image;           // Ảnh thumbnail
    private BigDecimal price;       // Giá bán
    private BigDecimal originalPrice; // Giá gốc
    private String discountLabel;   // VD: "Giảm 1.5tr"
    private String configSummary;   // VD: "8GB/128GB" (để hiện ngay trên card cho tiện)
}

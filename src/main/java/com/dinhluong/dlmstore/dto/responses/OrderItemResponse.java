package com.dinhluong.dlmstore.dto.responses;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderItemResponse {
   private Long id;
    private Long productVariantId; 
    
    // 🔥 Thêm trường productName để lưu tên sản phẩm chính
    private String productName;
    
    // 🔥 (Tùy chọn) Thêm variantName nếu bạn muốn lưu tên phân loại (vd: "Đen, 256GB")
    private String variantName;
    
    private String imageUrl;    
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private List<ComboItemDetail> comboItems;
}

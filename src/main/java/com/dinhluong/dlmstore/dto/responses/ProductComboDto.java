package com.dinhluong.dlmstore.dto.responses;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductComboDto {
    private Long id;           // ID của record Combo (để add vào giỏ hàng)
    private Long relatedProductId; // ID sản phẩm mua kèm
    private String name;       // Tên sản phẩm mua kèm
    private String image;      // Ảnh thumbnail
    
    // Các trường hiển thị giá (Đã format sang String: "100.000đ")
    private String price;      // Giá sau khi giảm (Giá mua kèm)
    private String oldPrice;   // Giá gốc
    private String saving;     // Text hiển thị tiết kiệm (VD: "-50.000đ")
    
    // Các trường raw để Frontend tính toán tổng tiền nếu cần
    private BigDecimal rawPrice; 
    private BigDecimal rawDiscount;
}
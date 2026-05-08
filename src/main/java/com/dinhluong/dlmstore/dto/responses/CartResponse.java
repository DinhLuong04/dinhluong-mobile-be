package com.dinhluong.dlmstore.dto.responses;



import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    
   
    private List<CartItemDto> items;
    @Data
    public static class CartItemDto {
        // Thông tin định danh 
        private Long id; 
        private Long productVariantId;            
        private String sku;         
        // Thông tin hiển thị 
        private String name;        
        private String slug;        
        private String image;       

        // Thông tin giá & Biến thể 
        private BigDecimal price;       
        private BigDecimal originalPrice;
        private String colorName;   
        private String rom;      

        // Trạng thái (UI State) 
        private Integer quantity;   
        private Boolean checked = true; 
        private Integer stockQuantity;
        private List<CartComboItemDto> combos;
    }

    @Data
    public static class CartComboItemDto {
        private Long id;            
        private String name;         
        private String image;       
        private BigDecimal price;       
        private BigDecimal originalPrice; 
        private Boolean checked =false;
        private BigDecimal discountAmount;
        private Integer stockQuantity;
        private String note;    
    }
}
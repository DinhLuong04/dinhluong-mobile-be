package com.dinhluong.dlmstore.dto.responses;



import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCardResponse {
    private Long id;
    private String slug;              // Map từ 'slug' (VD: iphone-17-pro-max)
    private String name;            // VD: iPhone 17 Pro Max
    private String image;           // Map từ 'thumbnailUrl'
    private BigDecimal price;       // Map từ 'displayPrice'
    private BigDecimal originalPrice;
    private String discountNote;    // Logic: "Giảm 300.000đ"
    private String installmentText; // VD: "Trả góp 0%"
    
    // --- Các field danh sách ---
    private List<SpecDto> specs;    // Lấy từ bảng product_highlight_specs
    private List<ColorDto> colors;  // Lấy danh sách màu duy nhất từ variants
    private List<VariantDto> variants; // Lấy danh sách ROM duy nhất từ variants
    
    // --- Khuyến mãi ---
    private List<String> promotions; // List ảnh logo khuyến mãi (Lấy từ field JSON hoặc fix cứng)
    private String promotionText;    // Lấy từ description hoặc 1 field tóm tắt
    
    // --- Inner DTOs ---
    @Data
    public static class SpecDto {
        private String icon;
        private String label;
        private String subLabel; // Map từ 'value' trong DB
    }

    @Data
    public static class ColorDto {
        private String hex;
    }
    
    @Data
    public static class VariantDto {
        private String label; // VD: "256 GB"
        private boolean active; // Mặc định cái đầu tiên là true
    }
}

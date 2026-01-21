package com.dinhluong.dlmstore.dto.responses;



import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailResponse {
    // --- Thông tin cơ bản ---
    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;          // Giá hiện tại (thấp nhất)
    private BigDecimal originalPrice;  // Giá gốc
    private String discountNote;       // "Giảm 300.000đ"
    private String installmentText;    // "Trả góp 0%"
    private String description;        // HTML mô tả
    private String thumbnail;
    // --- Ảnh Slider (productImages) ---
    private List<String> productImages;

    // --- Thông số nổi bật (productSpecs - 3 icon đầu) ---
    private List<HighlightSpecDto> highlightSpecs;

    // --- Tùy chọn (Cho user click chọn) ---
    private List<String> storageOptions;      // ["256 GB", "512 GB"...]
    private List<ColorOptionDto> colorOptions;// [{name: "Cam", img: "..."}]

    // --- Danh sách biến thể thực tế (Để JS check tồn kho/giá) ---
    private List<VariantDetailDto> variants;

    // --- Thông số kỹ thuật chi tiết (specsData - Bảng to) ---
    private List<SpecGroupDto> specsData;

    // --- Thông tin tĩnh (Promotions & Policies) ---
    private List<String> promotions;

    // ================= INNER CLASSES =================

    @Data
    public static class HighlightSpecDto {
        private String label;    // "Màn hình"
        private String value;    // "6.9 inch"
        private String icon;     // URL icon
    }

    @Data
    public static class ColorOptionDto {
        private String name;      // "Cam Vũ Trụ"
        private String hex;       // "#FA8C4A"
        private String img;       // Ảnh đại diện cho màu này
    }

    @Data
    public static class VariantDetailDto {
        private String sku;
        private String rom;
        private String colorName;
        private BigDecimal price;
        private Integer stock;
    }

    @Data
    public static class SpecGroupDto {
        private Long id;
        private String title;     // "Bộ xử lý"
        private List<SpecItemDto> items;
    }

    @Data
    public static class SpecItemDto {
        private String label;     // "Loại CPU"
        private String value;     // "12-Core"
    }

    
}

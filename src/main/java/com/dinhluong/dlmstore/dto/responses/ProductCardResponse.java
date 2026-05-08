package com.dinhluong.dlmstore.dto.responses;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCardResponse {
    private Long id;
    private String slug;
    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discountNote;
    private String installmentText;

    // Các field danh sách
    private List<SpecDto> specs;
    private List<ColorDto> colors;
    private List<VariantDto> variants;

    // Khuyến mãi
    private List<String> promotions;
    private String promotionText;

    @Data
    public static class SpecDto {
        private String icon;
        private String label;
        private String subLabel;
    }

    @Data
    public static class ColorDto {
        private String hex;
    }

    @Data
    public static class VariantDto {
        private String label;
        private boolean active;
    }
}

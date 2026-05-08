package com.dinhluong.dlmstore.dto.responses;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailResponse {
    //  Thông tin cơ bản 
    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discountNote;
    private String installmentText;
    private String description;
    private String thumbnail;
    private List<String> productImages;
    private List<HighlightSpecDto> highlightSpecs;
    private List<String> storageOptions;
    private List<ColorOptionDto> colorOptions;
    private List<VariantDetailDto> variants;
    private List<SpecGroupDto> specsData;
    private List<String> promotions;

    @Data
    public static class HighlightSpecDto {
        private String label;
        private String value;
        private String icon;
    }

    @Data
    public static class ColorOptionDto {
        private String name;
        private String hex;
        private String img;
    }

    @Data
    public static class VariantDetailDto {
        private Long id;
        private String sku;
        private String rom;
        private String colorName;
        private BigDecimal price;
        private Integer stock;
    }

    @Data
    public static class SpecGroupDto {
        private Long id;
        private String title;
        private List<SpecItemDto> items;
    }

    @Data
    public static class SpecItemDto {
        private String label;
        private String value;
    }

}

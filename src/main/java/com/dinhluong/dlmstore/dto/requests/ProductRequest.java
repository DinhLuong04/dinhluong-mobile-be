package com.dinhluong.dlmstore.dto.requests;

import com.dinhluong.dlmstore.entity.Enums.OsType;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

@Data
public class ProductRequest {
    private Long id;
    private String name;
    private String slug;
    private ProductType productType;
    private Long categoryId;
    private Long brandId;
    private BigDecimal displayPrice;
    private BigDecimal originalPrice;
    private ProductStatus status;
    private String description;
    private String thumbnailUrl;
    private JsonNode specificationsJson;
    private String installmentText;
    private String highlightFeatures;
    private String specialFeatures;
    private OsType osType;
    private Double screenSize;
    private String screenResolutionType;
    private Integer refreshRate;
    private Integer batteryCapacity;
    private Boolean support5g;

    private List<ImageDTO> images;
    private List<VariantDTO> variants;
    private List<HighlightSpecDTO> highlightSpecs;
    private List<SpecValueDTO> specValues;

    @Data
    public static class ImageDTO {
        private Long id;
        private String imageUrl;
        private Integer sortOrder;
    }

    @Data
    public static class VariantDTO {
        private Long id;
        private String sku;
        private String colorName;
        private String colorHex;
        private BigDecimal price;
        private String ram;
        private String rom;
        private Integer stockQuantity;
        private Boolean isActive;

        private String imageUrl;
    }

    @Data
    public static class HighlightSpecDTO {
        private Long id;
        private String label;
        private String value;
        private String iconUrl;
    }

    @Data
    public static class SpecValueDTO {
        private Long attributeId;
        private String value;
    }
}
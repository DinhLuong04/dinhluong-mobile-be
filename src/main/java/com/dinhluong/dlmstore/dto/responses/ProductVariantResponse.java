package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private String colorName;
    private String colorHex;
    private String ram;
    private String rom;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
}
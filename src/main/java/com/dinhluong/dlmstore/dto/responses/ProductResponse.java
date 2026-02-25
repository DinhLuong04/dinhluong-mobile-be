package com.dinhluong.dlmstore.dto.responses;



import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private BigDecimal displayPrice;
    private BigDecimal originalPrice;
    private String thumbnailUrl;
    private String brandName;
    private String categoryName;
    private String status;
    private Integer totalVariants; // Số lượng phiên bản
    private LocalDateTime createdAt;
    private Integer totalStock; 
    private Boolean isFeatured; 
    private Integer soldQuantity;
    private Integer lowStockVariantCount;
    private Integer outOfStockVariantCount;
}
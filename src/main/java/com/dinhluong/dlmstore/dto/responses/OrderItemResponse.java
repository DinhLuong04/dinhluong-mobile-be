package com.dinhluong.dlmstore.dto.responses;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productVariantId;
    private String slug;
    private String productName;
    private String variantName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private List<ComboItemDetail> comboItems;
    private boolean isAvailable;
}

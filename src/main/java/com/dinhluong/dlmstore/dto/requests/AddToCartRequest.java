package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;
import java.util.List;

@Data
public class AddToCartRequest {
    private Long productVariantId;
    private Integer quantity;
    private List<Long> comboVariantIds;
}
package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductComboRequest {
    private Long mainProductId;
    private Long relatedProductId;
    private BigDecimal discountAmount;
    private String note;
}
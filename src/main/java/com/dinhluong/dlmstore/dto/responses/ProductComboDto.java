package com.dinhluong.dlmstore.dto.responses;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductComboDto {
    private Long id;
    private Long relatedProductId;
    private String name;
    private String image;
    private String price;
    private String oldPrice;
    private String saving;
    private BigDecimal rawPrice;
    private BigDecimal rawDiscount;
}
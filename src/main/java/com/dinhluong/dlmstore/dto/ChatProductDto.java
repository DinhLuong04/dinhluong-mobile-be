package com.dinhluong.dlmstore.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ChatProductDto {
    private Long id;
    private String name;
    private String slug;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discountLabel;
    private String configSummary;
}

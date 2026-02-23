package com.dinhluong.dlmstore.dto.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboItemDetail {
    private Long variantId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
}

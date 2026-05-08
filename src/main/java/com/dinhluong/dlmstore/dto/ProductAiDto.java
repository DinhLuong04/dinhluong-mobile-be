package com.dinhluong.dlmstore.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductAiDto {
    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private List<String> inventory;
    private Map<String, String> specs;
    private String promotion;
}
